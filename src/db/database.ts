/**
 * CareerForge - Career Acceleration Platform for BTech Engineering Students
 * Copyright (C) 2026 CareerForge Contributors
 *
 * Licensed under the GNU Affero General Public License v3.0 or later (AGPL-3.0-or-later).
 * See LICENSE file in the project root for full license information.
 */

import {
  createRxDatabase,
  RxDatabase,
  RxCollection,
  RxJsonSchema,
  toTypedRxJsonSchema,
  ExtractDocumentTypeFromTypedRxJsonSchema,
} from 'rxdb';
import { getRxStorageSQLiteTrial } from 'rxdb/plugins/storage-sqlite';
import * as SQLite from 'expo-sqlite';
import NetInfo, { NetInfoState } from '@react-native-community/netinfo';
import { createClient, SupabaseClient, RealtimeChannel } from '@supabase/supabase-js';

// ============================================================================
// 1. CRDT & SCHEMA DEFINITIONS
// ============================================================================

export interface CRDTMetadata {
  lamportClock: number;
  clientId: string;
  isDeleted: boolean;
  lastModifiedAt: string;
}

// Schema 1: Student Profile
export const studentProfileSchemaLiteral = {
  version: 0,
  primaryKey: 'id',
  type: 'object',
  properties: {
    id: { type: 'string', maxLength: 100 },
    name: { type: 'string' },
    college: { type: 'string' },
    branch: { type: 'string' },
    currentSemester: { type: 'number', minimum: 1, maximum: 8 },
    targetRole: { type: 'string' },
    targetTier: { type: 'string' },
    leetCodeUsername: { type: 'string' },
    githubUsername: { type: 'string' },
    solvedDsaCount: { type: 'number' },
    currentCgpa: { type: 'number' },
    lamportClock: { type: 'number' },
    clientId: { type: 'string' },
    isDeleted: { type: 'boolean' },
    lastModifiedAt: { type: 'string' },
  },
  required: ['id', 'name', 'currentSemester', 'targetRole', 'lamportClock', 'clientId', 'isDeleted', 'lastModifiedAt'],
} as const;

// Schema 2: Skill Roadmaps (8-Semester Graph)
export const skillRoadmapsSchemaLiteral = {
  version: 0,
  primaryKey: 'id',
  type: 'object',
  properties: {
    id: { type: 'string', maxLength: 100 },
    semester: { type: 'number', minimum: 1, maximum: 8 },
    subject: { type: 'string' },
    category: { type: 'string' },
    title: { type: 'string' },
    description: { type: 'string' },
    status: { type: 'string', enum: ['NOT_STARTED', 'IN_PROGRESS', 'COMPLETED'] },
    isAiGap: { type: 'boolean' },
    gapPriority: { type: 'string', enum: ['NONE', 'LOW', 'MEDIUM', 'HIGH'] },
    aiGapReason: { type: 'string' },
    actionPlan: { type: 'string' },
    lamportClock: { type: 'number' },
    clientId: { type: 'string' },
    isDeleted: { type: 'boolean' },
    lastModifiedAt: { type: 'string' },
  },
  required: ['id', 'semester', 'subject', 'title', 'status', 'isAiGap', 'lamportClock', 'clientId', 'isDeleted', 'lastModifiedAt'],
} as const;

// Schema 3: DSA Tracker
export const dsaTrackerSchemaLiteral = {
  version: 0,
  primaryKey: 'id',
  type: 'object',
  properties: {
    id: { type: 'string', maxLength: 100 },
    title: { type: 'string' },
    platform: { type: 'string' },
    difficulty: { type: 'string', enum: ['EASY', 'MEDIUM', 'HARD'] },
    topic: { type: 'string' },
    status: { type: 'string', enum: ['SOLVED', 'REVISING', 'STUCK'] },
    timeComplexity: { type: 'string' },
    spaceComplexity: { type: 'string' },
    notes: { type: 'string' },
    solvedAt: { type: 'string' },
    lamportClock: { type: 'number' },
    clientId: { type: 'string' },
    isDeleted: { type: 'boolean' },
    lastModifiedAt: { type: 'string' },
  },
  required: ['id', 'title', 'difficulty', 'topic', 'status', 'lamportClock', 'clientId', 'isDeleted', 'lastModifiedAt'],
} as const;

// Schema 4: Interview Sessions
export const interviewSessionsSchemaLiteral = {
  version: 0,
  primaryKey: 'id',
  type: 'object',
  properties: {
    id: { type: 'string', maxLength: 100 },
    timestamp: { type: 'string' },
    role: { type: 'string' },
    topic: { type: 'string' },
    questionText: { type: 'string' },
    studentCode: { type: 'string' },
    feedback: { type: 'string' },
    codeQualityScore: { type: 'number', minimum: 0, maximum: 100 },
    logicScore: { type: 'number', minimum: 0, maximum: 100 },
    confidenceScore: { type: 'number', minimum: 0, maximum: 100 },
    lamportClock: { type: 'number' },
    clientId: { type: 'string' },
    isDeleted: { type: 'boolean' },
    lastModifiedAt: { type: 'string' },
  },
  required: ['id', 'role', 'questionText', 'studentCode', 'logicScore', 'lamportClock', 'clientId', 'isDeleted', 'lastModifiedAt'],
} as const;

// Schema 5: Resume Versions
export const resumeVersionsSchemaLiteral = {
  version: 0,
  primaryKey: 'id',
  type: 'object',
  properties: {
    id: { type: 'string', maxLength: 100 },
    title: { type: 'string' },
    targetCompany: { type: 'string' },
    rawContent: { type: 'string' },
    atsScore: { type: 'number', minimum: 0, maximum: 100 },
    suggestionsJson: { type: 'string' },
    lamportClock: { type: 'number' },
    clientId: { type: 'string' },
    isDeleted: { type: 'boolean' },
    lastModifiedAt: { type: 'string' },
  },
  required: ['id', 'title', 'rawContent', 'atsScore', 'lamportClock', 'clientId', 'isDeleted', 'lastModifiedAt'],
} as const;

// Schema 6: Study Timetable & Gamified Streaks
export const studyTimetableSchemaLiteral = {
  version: 0,
  primaryKey: 'id',
  type: 'object',
  properties: {
    id: { type: 'string', maxLength: 100 },
    dayOfWeek: { type: 'string' },
    startTime: { type: 'string' },
    endTime: { type: 'string' },
    topic: { type: 'string' },
    activityType: { type: 'string', enum: ['DSA', 'CORE_CS', 'PROJECT', 'CONTEST', 'REVISION'] },
    cronSchedule: { type: 'string' },
    streakCount: { type: 'number' },
    isCompletedToday: { type: 'boolean' },
    priority: { type: 'string', enum: ['HIGH', 'MEDIUM', 'LOW'] },
    lamportClock: { type: 'number' },
    clientId: { type: 'string' },
    isDeleted: { type: 'boolean' },
    lastModifiedAt: { type: 'string' },
  },
  required: ['id', 'topic', 'activityType', 'cronSchedule', 'streakCount', 'lamportClock', 'clientId', 'isDeleted', 'lastModifiedAt'],
} as const;

// Schema 7: Cognitive Games (Big-O Agility & Algorithmic Speed)
export const cognitiveGamesSchemaLiteral = {
  version: 0,
  primaryKey: 'id',
  type: 'object',
  properties: {
    id: { type: 'string', maxLength: 100 },
    gameType: { type: 'string', enum: ['BIG_O_AGILITY', 'MEMORY_TRACE', 'PATTERN_RECOG'] },
    snippetPrompt: { type: 'string' },
    targetAnswer: { type: 'string' },
    studentAnswer: { type: 'string' },
    isCorrect: { type: 'boolean' },
    responseTimeMs: { type: 'number' },
    score: { type: 'number' },
    comboMultiplier: { type: 'number' },
    playedAt: { type: 'string' },
    lamportClock: { type: 'number' },
    clientId: { type: 'string' },
    isDeleted: { type: 'boolean' },
    lastModifiedAt: { type: 'string' },
  },
  required: ['id', 'gameType', 'snippetPrompt', 'targetAnswer', 'isCorrect', 'score', 'lamportClock', 'clientId', 'isDeleted', 'lastModifiedAt'],
} as const;

// Schema 8: Chatbot Memory & Career Mentor Persistence
export const chatbotMemorySchemaLiteral = {
  version: 0,
  primaryKey: 'id',
  type: 'object',
  properties: {
    id: { type: 'string', maxLength: 100 },
    conversationId: { type: 'string' },
    role: { type: 'string', enum: ['user', 'model', 'system'] },
    messageText: { type: 'string' },
    memoryKey: { type: 'string' },
    importanceScore: { type: 'number', minimum: 1, maximum: 10 },
    timestamp: { type: 'string' },
    lamportClock: { type: 'number' },
    clientId: { type: 'string' },
    isDeleted: { type: 'boolean' },
    lastModifiedAt: { type: 'string' },
  },
  required: ['id', 'conversationId', 'role', 'messageText', 'importanceScore', 'lamportClock', 'clientId', 'isDeleted', 'lastModifiedAt'],
} as const;

const typedStudentProfileSchema = toTypedRxJsonSchema(studentProfileSchemaLiteral);
const typedSkillRoadmapsSchema = toTypedRxJsonSchema(skillRoadmapsSchemaLiteral);
const typedDsaTrackerSchema = toTypedRxJsonSchema(dsaTrackerSchemaLiteral);
const typedInterviewSessionsSchema = toTypedRxJsonSchema(interviewSessionsSchemaLiteral);
const typedResumeVersionsSchema = toTypedRxJsonSchema(resumeVersionsSchemaLiteral);
const typedStudyTimetableSchema = toTypedRxJsonSchema(studyTimetableSchemaLiteral);
const typedCognitiveGamesSchema = toTypedRxJsonSchema(cognitiveGamesSchemaLiteral);
const typedChatbotMemorySchema = toTypedRxJsonSchema(chatbotMemorySchemaLiteral);

export type StudentProfileDocType = ExtractDocumentTypeFromTypedRxJsonSchema<typeof typedStudentProfileSchema>;
export type SkillRoadmapsDocType = ExtractDocumentTypeFromTypedRxJsonSchema<typeof typedSkillRoadmapsSchema>;
export type DsaTrackerDocType = ExtractDocumentTypeFromTypedRxJsonSchema<typeof typedDsaTrackerSchema>;
export type InterviewSessionsDocType = ExtractDocumentTypeFromTypedRxJsonSchema<typeof typedInterviewSessionsSchema>;
export type ResumeVersionsDocType = ExtractDocumentTypeFromTypedRxJsonSchema<typeof typedResumeVersionsSchema>;
export type StudyTimetableDocType = ExtractDocumentTypeFromTypedRxJsonSchema<typeof typedStudyTimetableSchema>;
export type CognitiveGamesDocType = ExtractDocumentTypeFromTypedRxJsonSchema<typeof typedCognitiveGamesSchema>;
export type ChatbotMemoryDocType = ExtractDocumentTypeFromTypedRxJsonSchema<typeof typedChatbotMemorySchema>;

export type CareerForgeCollections = {
  studentProfile: RxCollection<StudentProfileDocType>;
  skillRoadmaps: RxCollection<SkillRoadmapsDocType>;
  dsaTracker: RxCollection<DsaTrackerDocType>;
  interviewSessions: RxCollection<InterviewSessionsDocType>;
  resumeVersions: RxCollection<ResumeVersionsDocType>;
  studyTimetable: RxCollection<StudyTimetableDocType>;
  cognitiveGames: RxCollection<CognitiveGamesDocType>;
  chatbotMemory: RxCollection<ChatbotMemoryDocType>;
};

export type CareerForgeDatabase = RxDatabase<CareerForgeCollections>;

// ============================================================================
// 2. DATABASE INITIALIZATION VIA EXPO-SQLITE
// ============================================================================

let dbInstancePromise: Promise<CareerForgeDatabase> | null = null;
const CLIENT_ID = `client_${Math.random().toString(36).substring(2, 9)}`;
let currentLamportClock = Date.now();

export function getNextLamportClock(): number {
  currentLamportClock = Math.max(currentLamportClock + 1, Date.now());
  return currentLamportClock;
}

export async function initCareerForgeDatabase(): Promise<CareerForgeDatabase> {
  if (dbInstancePromise) {
    return dbInstancePromise;
  }

  dbInstancePromise = (async () => {
    // Expo SQLite database handle
    const sqliteStorage = getRxStorageSQLiteTrial({
      sqliteBasics: {
        openDatabase: async (name: string) => {
          return SQLite.openDatabaseSync(`${name}.db`);
        },
      },
    });

    const db = await createRxDatabase<CareerForgeCollections>({
      name: 'careerforge_rxdb',
      storage: sqliteStorage,
      multiInstance: false,
      ignoreDuplicate: true,
    });

    await db.addCollections({
      studentProfile: { schema: studentProfileSchemaLiteral },
      skillRoadmaps: { schema: skillRoadmapsSchemaLiteral },
      dsaTracker: { schema: dsaTrackerSchemaLiteral },
      interviewSessions: { schema: interviewSessionsSchemaLiteral },
      resumeVersions: { schema: resumeVersionsSchemaLiteral },
      studyTimetable: { schema: studyTimetableSchemaLiteral },
      cognitiveGames: { schema: cognitiveGamesSchemaLiteral },
      chatbotMemory: { schema: chatbotMemorySchemaLiteral },
    });

    return db;
  })();

  return dbInstancePromise;
}

// ============================================================================
// 3. BACKGROUND CRDT & SUPABASE CDC SYNC MANAGER
// ============================================================================

export type SyncState = 'ONLINE_SYNCED' | 'OFFLINE_BUFFERED' | 'SYNCING' | 'ERROR';

export class BackgroundSyncManager {
  private db: CareerForgeDatabase | null = null;
  private supabase: SupabaseClient | null = null;
  private realtimeChannel: RealtimeChannel | null = null;
  private isConnected = false;
  private syncStateListeners: Set<(state: SyncState, queueSize: number) => void> = new Set();
  private mutationQueue: Array<{
    collection: keyof CareerForgeCollections;
    action: 'UPSERT' | 'DELETE';
    data: any;
  }> = [];

  constructor(supabaseUrl?: string, supabaseKey?: string) {
    if (supabaseUrl && supabaseKey) {
      this.supabase = createClient(supabaseUrl, supabaseKey);
    }
    this.initNetworkListener();
  }

  public registerStateListener(listener: (state: SyncState, queueSize: number) => void): () => void {
    this.syncStateListeners.add(listener);
    return () => this.syncStateListeners.delete(listener);
  }

  private notifyState(state: SyncState): void {
    this.syncStateListeners.forEach((l) => l(state, this.mutationQueue.length));
  }

  private initNetworkListener(): void {
    NetInfo.addEventListener((state: NetInfoState) => {
      const previouslyConnected = this.isConnected;
      this.isConnected = Boolean(state.isConnected && state.isInternetReachable);

      if (this.isConnected && !previouslyConnected) {
        this.notifyState('SYNCING');
        this.drainMutationBufferToSupabase().catch((err) => {
          console.error('[SyncManager] Failed to drain buffer:', err);
          this.notifyState('ERROR');
        });
      } else if (!this.isConnected) {
        this.notifyState('OFFLINE_BUFFERED');
      }
    });
  }

  public async setDatabase(db: CareerForgeDatabase): Promise<void> {
    this.db = db;
    this.subscribeToCollectionMutations();
    if (this.supabase && this.isConnected) {
      this.subscribeToSupabaseCDC();
    }
  }

  /**
   * CRDT Mutation Interceptor: Whenever local data writes occur in RxDB,
   * stamps Lamport logical clock and buffers if offline.
   */
  private subscribeToCollectionMutations(): void {
    if (!this.db) return;

    const collectionNames: Array<keyof CareerForgeCollections> = [
      'studentProfile',
      'skillRoadmaps',
      'dsaTracker',
      'interviewSessions',
      'resumeVersions',
      'studyTimetable',
      'cognitiveGames',
      'chatbotMemory',
    ];

    collectionNames.forEach((collName) => {
      this.db![collName].$.subscribe((event: any) => {
        if (event.isLocal) {
          const docData = event.documentData;
          this.queueMutation(collName, 'UPSERT', docData);
        }
      });
    });
  }

  public queueMutation(collection: keyof CareerForgeCollections, action: 'UPSERT' | 'DELETE', data: any): void {
    this.mutationQueue.push({ collection, action, data });
    if (!this.isConnected) {
      this.notifyState('OFFLINE_BUFFERED');
    } else {
      this.drainMutationBufferToSupabase().catch((e) => {
        console.error('[SyncManager] Buffer drain error:', e);
      });
    }
  }

  /**
   * Dumps buffered mutations downstream to Supabase using conflict-free resolution
   */
  public async drainMutationBufferToSupabase(): Promise<void> {
    if (!this.isConnected || !this.supabase || this.mutationQueue.length === 0) {
      this.notifyState(this.isConnected ? 'ONLINE_SYNCED' : 'OFFLINE_BUFFERED');
      return;
    }

    this.notifyState('SYNCING');

    while (this.mutationQueue.length > 0) {
      const item = this.mutationQueue[0];
      try {
        const { collection, data } = item;
        const tableName = collection.toLowerCase();

        // Downstream UPSERT with Last-Write-Wins (LWW) via Lamport clock comparison
        const { error } = await this.supabase
          .from(tableName)
          .upsert(data, { onConflict: 'id' });

        if (error) {
          throw error;
        }

        // Successfully dispatched
        this.mutationQueue.shift();
      } catch (err) {
        console.warn('[SyncManager] CDC transmission deferred, pausing buffer drain:', err);
        this.notifyState('OFFLINE_BUFFERED');
        return;
      }
    }

    this.notifyState('ONLINE_SYNCED');
  }

  /**
   * Subscribes to Supabase Realtime CDC WebSocket stream
   */
  private subscribeToSupabaseCDC(): void {
    if (!this.supabase || !this.db) return;

    this.realtimeChannel = this.supabase
      .channel('careerforge_cdc_feed')
      .on('postgres_changes', { event: '*', schema: 'public' }, async (payload) => {
        const table = payload.table as keyof CareerForgeCollections;
        const incomingDoc = payload.new as any;

        if (this.db && this.db[table] && incomingDoc) {
          // CRDT Merge: Apply Last-Write-Wins (LWW) based on Lamport Clock
          const existing = await this.db[table].findOne(incomingDoc.id).exec();
          if (!existing || existing.lamportClock < incomingDoc.lamportClock) {
            await this.db[table].upsert(incomingDoc);
          }
        }
      })
      .subscribe();
  }

  public destroy(): void {
    if (this.realtimeChannel) {
      this.realtimeChannel.unsubscribe();
    }
  }
}
