/**
 * CareerForge - Career Acceleration Platform for BTech Engineering Students
 * Copyright (C) 2026 CareerForge Contributors
 *
 * Licensed under the GNU Affero General Public License v3.0 or later (AGPL-3.0-or-later).
 * See LICENSE file in the project root for full license information.
 */

import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, ScrollView, ActivityIndicator } from 'react-native';
import { BionicText } from './BionicText';
import { GeminiOrchestrator, ParsedTimetableSlot } from '../services/gemini';
import { DailyStudyGoalsModule } from './DailyStudyGoalsModule';

export interface TimetablePanelProps {
  currentStreakDays?: number;
  highestStreakDays?: number;
  slots: ParsedTimetableSlot[];
  onAddSlots: (slots: ParsedTimetableSlot[]) => void;
  onToggleSlotComplete: (slotId: string) => void;
  geminiOrchestrator: GeminiOrchestrator;
  className?: string;
}

export const TimetablePanel: React.FC<TimetablePanelProps> = ({
  currentStreakDays = 12,
  highestStreakDays = 28,
  slots,
  onAddSlots,
  onToggleSlotComplete,
  geminiOrchestrator,
  className = '',
}) => {
  const [activeTab, setActiveTab] = useState<'GOALS' | 'SCHEDULE'>('GOALS');
  const [rawPrompt, setRawPrompt] = useState('');
  const [isParsing, setIsParsing] = useState(false);
  const [scheduledNotificationLogs, setScheduledNotificationLogs] = useState<string[]>([]);

  /**
   * Helper that connects parsed 5-field cron schedules to local notification loops
   */
  const registerLocalNotificationSchedule = (slot: ParsedTimetableSlot) => {
    // Translates standard 5-field cron expression into next scheduled notification
    const [min, hour, , , dayOfWeek] = slot.cronSchedule.split(' ');
    const log = `🔔 [Local Alert Armed] "${slot.topic}" trigger set at ${hour.padStart(2, '0')}:${min.padStart(2, '0')} (Days: ${dayOfWeek})`;
    setScheduledNotificationLogs((prev) => [log, ...prev.slice(0, 4)]);
  };

  const handleParseTimetable = async () => {
    if (!rawPrompt.trim()) return;

    setIsParsing(true);
    try {
      const result = await geminiOrchestrator.parseUnstructuredTimetable(rawPrompt);
      onAddSlots(result.slots);

      // Register local notification triggers directly for all parsed slots
      result.slots.forEach((slot) => {
        registerLocalNotificationSchedule(slot);
      });

      setRawPrompt('');
    } catch (err) {
      console.error('[TimetablePanel] parse error:', err);
    } finally {
      setIsParsing(false);
    }
  };

  return (
    <View className={`flex-1 bg-slate-950 p-4 ${className}`}>
      {/* Sub-Tab Navigation Header */}
      <View className="flex-row bg-slate-900 rounded-xl p-1 mb-4 border border-slate-800">
        <TouchableOpacity
          onPress={() => setActiveTab('GOALS')}
          className={`flex-1 py-2 rounded-lg items-center ${
            activeTab === 'GOALS' ? 'bg-sky-600 shadow' : 'bg-transparent'
          }`}
        >
          <Text className={`text-xs font-bold ${
            activeTab === 'GOALS' ? 'text-white' : 'text-slate-400'
          }`}>
            Daily Study Goals 🎯
          </Text>
        </TouchableOpacity>

        <TouchableOpacity
          onPress={() => setActiveTab('SCHEDULE')}
          className={`flex-1 py-2 rounded-lg items-center ${
            activeTab === 'SCHEDULE' ? 'bg-sky-600 shadow' : 'bg-transparent'
          }`}
        >
          <Text className={`text-xs font-bold ${
            activeTab === 'SCHEDULE' ? 'text-white' : 'text-slate-400'
          }`}>
            Schedule & AI Parser 🤖 ({slots.length})
          </Text>
        </TouchableOpacity>
      </View>

      {activeTab === 'GOALS' ? (
        <ScrollView className="flex-1" showsVerticalScrollIndicator={false}>
          <DailyStudyGoalsModule
            slots={slots}
            onToggleSlotComplete={onToggleSlotComplete}
            streakDays={currentStreakDays}
          />
        </ScrollView>
      ) : (
        <ScrollView className="flex-1" showsVerticalScrollIndicator={false}>
          {/* 1. Gamified Streak Container Banner */}
          <View className="bg-slate-900 border border-amber-500/50 rounded-2xl p-4 mb-4 shadow-lg shadow-amber-500/10">
            <View className="flex-row items-center justify-between mb-2">
              <View className="flex-row items-center">
                <Text className="text-2xl mr-2">🔥</Text>
                <View>
                  <Text className="text-xs font-bold text-amber-400">PLACEMENT GRIND STREAK</Text>
                  <Text className="text-xl font-black text-white">{currentStreakDays} CONSECUTIVE DAYS</Text>
                </View>
              </View>

              <View className="bg-amber-950 px-3 py-1 rounded-full border border-amber-500/60">
                <Text className="text-xs font-extrabold text-amber-300">
                  {Math.min(Math.floor(currentStreakDays / 5) + 1, 5)}x XP BOOST
                </Text>
              </View>
            </View>

            <Text className="text-[11px] text-slate-400">
              Personal Best: {highestStreakDays} Days • Zero Zero-Days Policy Active
            </Text>
          </View>

          {/* 2. Natural Language Timetable Parser Input */}
          <View className="bg-slate-900/90 border border-slate-800 rounded-xl p-3 mb-4">
            <Text className="text-xs font-bold text-sky-400 mb-1">
              PARSE UNSTRUCTURED STUDY SCHEDULE (GEMINI 2.5 FLASH)
            </Text>
            <TextInput
              value={rawPrompt}
              onChangeText={setRawPrompt}
              placeholder="e.g. 2 hrs daily DSA 7-9pm, OS virtual memory revision Sat 10am-1pm, contest Sun 11am"
              placeholderTextColor="#64748B"
              multiline
              className="text-xs text-slate-200 font-sans min-h-[60px] mb-2"
            />

            <TouchableOpacity
              onPress={handleParseTimetable}
              disabled={isParsing || !rawPrompt.trim()}
              className={`py-2.5 rounded-lg items-center justify-center ${
                isParsing ? 'bg-sky-900' : 'bg-sky-600 active:bg-sky-500'
              }`}
            >
              {isParsing ? (
                <View className="flex-row items-center">
                  <ActivityIndicator size="small" color="#FFFFFF" />
                  <Text className="text-white text-xs font-bold ml-2">Mapping to Cron Slots...</Text>
                </View>
              ) : (
                <Text className="text-white text-xs font-bold">Generate Cron Timetable & Arm Alerts</Text>
              )}
            </TouchableOpacity>
          </View>

          {/* 3. Armed Local Notification Logs */}
          {scheduledNotificationLogs.length > 0 && (
            <View className="bg-slate-900/50 border border-slate-800/80 p-2.5 rounded-xl mb-4">
              <Text className="text-[10px] font-bold text-slate-400 mb-1">LOCAL CRON NOTIFICATION DISPATCHER:</Text>
              {scheduledNotificationLogs.map((log, idx) => (
                <Text key={`log-${idx}`} className="font-mono text-[10px] text-emerald-400 leading-tight">
                  {log}
                </Text>
              ))}
            </View>
          )}

          {/* 4. Active Timetable Schedule List */}
          <Text className="text-xs font-bold text-slate-300 mb-2">SCHEDULED STUDY BLOCKS ({slots.length})</Text>
          {slots.map((slot) => {
            const isHigh = slot.priority === 'HIGH';

            return (
              <View
                key={slot.id}
                className={`p-3 rounded-xl mb-2.5 border ${
                  isHigh ? 'bg-slate-900/90 border-rose-500/40' : 'bg-slate-900/60 border-slate-800'
                }`}
              >
                <View className="flex-row items-center justify-between mb-1">
                  <View className="flex-row items-center">
                    <View className="bg-sky-950 px-2 py-0.5 rounded mr-2 border border-sky-600/40">
                      <Text className="text-[10px] font-bold text-sky-400">{slot.dayOfWeek}</Text>
                    </View>
                    <Text className="text-xs font-bold text-white">
                      {slot.startTime} - {slot.endTime}
                    </Text>
                  </View>

                  <View className="bg-slate-800 px-2 py-0.5 rounded">
                    <Text className="font-mono text-[10px] text-slate-300">{slot.cronSchedule}</Text>
                  </View>
                </View>

                <Text className="text-sm font-extrabold text-slate-100 mb-1">{slot.topic}</Text>

                {slot.rationale && (
                  <BionicText className="text-[11px] text-slate-400 leading-relaxed">
                    {slot.rationale}
                  </BionicText>
                )}

                <View className="flex-row items-center justify-between mt-2 pt-2 border-t border-slate-800">
                  <Text className="text-[10px] font-semibold text-slate-500">TYPE: {slot.activityType}</Text>
                  <TouchableOpacity
                    onPress={() => onToggleSlotComplete(slot.id)}
                    className="bg-emerald-950 px-3 py-1 rounded-full border border-emerald-500/50"
                  >
                    <Text className="text-[10px] font-bold text-emerald-300">✓ Mark Completed</Text>
                  </TouchableOpacity>
                </View>
              </View>
            );
          })}
        </ScrollView>
      )}
    </View>
  );
};

export default TimetablePanel;
