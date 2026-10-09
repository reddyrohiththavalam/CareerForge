/**
 * CareerForge - Career Acceleration Platform for BTech Engineering Students
 * Copyright (C) 2026 CareerForge Contributors
 *
 * Licensed under the GNU Affero General Public License v3.0 or later (AGPL-3.0-or-later).
 * See LICENSE file in the project root for full license information.
 */

import React, { useState, useEffect } from 'react';
import { View, Text, TouchableOpacity, Modal, ScrollView, ActivityIndicator } from 'react-native';
import { BionicText } from './BionicText';
import { GeminiOrchestrator } from '../services/gemini';

export interface PanicButtonProps {
  currentSemester: number;
  geminiOrchestrator: GeminiOrchestrator;
  className?: string;
}

export const PanicButton: React.FC<PanicButtonProps> = ({
  currentSemester,
  geminiOrchestrator,
  className = '',
}) => {
  const [isOpen, setIsOpen] = useState(false);
  const [selectedFriction, setSelectedFriction] = useState<string>('Exam in 3 days + Zero DSA');
  const [isLoading, setIsLoading] = useState(false);
  const [tasks, setTasks] = useState<Array<{ task: string; durationMins: number; rationale: string }>>([]);
  const [countdown, setCountdown] = useState(300); // 5 minutes anti-panic reset

  const frictionOptions = [
    'Exam in 3 days + Zero DSA',
    'Stuck on DP / Graphs for 4 hours',
    '20+ Off-campus rejections / ghosted',
    'Brain fog / ADHD paralysis',
    'Comparison with peers placed at 30+ LPA',
  ];

  useEffect(() => {
    let timer: NodeJS.Timeout;
    if (isOpen && countdown > 0) {
      timer = setInterval(() => setCountdown((prev) => Math.max(0, prev - 1)), 1000);
    }
    return () => clearInterval(timer);
  }, [isOpen, countdown]);

  const handleTriage = async () => {
    setIsLoading(true);
    try {
      const generated = await geminiOrchestrator.getPanicMicroTasks(selectedFriction, currentSemester);
      setTasks(generated);
      setCountdown(300);
    } catch (err) {
      console.error('[PanicButton] Triage error:', err);
    } finally {
      setIsLoading(false);
    }
  };

  const formatTimer = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs < 10 ? '0' : ''}${secs}`;
  };

  return (
    <>
      {/* Floating Panic Trigger Action */}
      <TouchableOpacity
        onPress={() => setIsOpen(true)}
        className={`bg-rose-600 active:bg-rose-700 px-4 py-3 rounded-full flex-row items-center justify-center shadow-lg shadow-rose-600/40 ${className}`}
      >
        <Text className="text-white text-base mr-2">🚨</Text>
        <Text className="text-white font-extrabold text-xs">Panic / Anti-Burnout</Text>
      </TouchableOpacity>

      {/* Immediate-Action Modal Interface */}
      <Modal visible={isOpen} transparent animationType="slide">
        <View className="flex-1 bg-black/80 justify-center items-center p-4">
          <View className="w-full max-w-lg bg-slate-900 border border-rose-500/80 rounded-2xl p-5">
            {/* Header */}
            <View className="flex-row justify-between items-center mb-3">
              <View className="flex-row items-center">
                <Text className="text-xl mr-2">🛡️</Text>
                <View>
                  <Text className="text-base font-extrabold text-white">Emergency Career Triage</Text>
                  <Text className="text-[11px] text-slate-400">Sem {currentSemester} • Anti-Burnout Protocol</Text>
                </View>
              </View>

              <TouchableOpacity
                onPress={() => setIsOpen(false)}
                className="w-8 h-8 rounded-full bg-slate-800 items-center justify-center"
              >
                <Text className="text-slate-400 font-bold">✕</Text>
              </TouchableOpacity>
            </View>

            {/* Friction Points Selector */}
            <Text className="text-xs font-semibold text-slate-300 mb-2">
              Select acute friction source:
            </Text>
            <ScrollView horizontal showsHorizontalScrollIndicator={false} className="flex-row mb-3">
              {frictionOptions.map((opt) => (
                <TouchableOpacity
                  key={opt}
                  onPress={() => setSelectedFriction(opt)}
                  className={`px-3 py-1.5 rounded-lg mr-2 border ${
                    selectedFriction === opt ? 'bg-rose-600 border-rose-400' : 'bg-slate-800 border-slate-700'
                  }`}
                >
                  <Text className="text-xs text-white font-medium">{opt}</Text>
                </TouchableOpacity>
              ))}
            </ScrollView>

            {/* Trigger Button */}
            <TouchableOpacity
              onPress={handleTriage}
              disabled={isLoading}
              className="bg-rose-600 active:bg-rose-700 py-3 rounded-xl items-center justify-center mb-4"
            >
              {isLoading ? (
                <View className="flex-row items-center">
                  <ActivityIndicator size="small" color="#FFFFFF" />
                  <Text className="text-white text-xs font-bold ml-2">Triaging with Gemini...</Text>
                </View>
              ) : (
                <Text className="text-white text-xs font-bold">Synthesize &lt;50 Words Micro-Tasks</Text>
              )}
            </TouchableOpacity>

            {/* Micro-Tasks Output in BionicText */}
            {tasks.length > 0 && (
              <View className="border-t border-slate-800 pt-3">
                <View className="flex-row justify-between items-center mb-2">
                  <Text className="text-[11px] font-bold text-rose-400">
                    RECOVERY SEQUENCE (&lt;50 WORDS TOTAL):
                  </Text>
                  <View className="bg-slate-800 px-2 py-0.5 rounded">
                    <Text className="text-sky-400 text-[10px] font-mono font-bold">
                      ⏱ {formatTimer(countdown)}
                    </Text>
                  </View>
                </View>

                {tasks.map((item, idx) => (
                  <View key={`task-${idx}`} className="bg-slate-800/80 p-3 rounded-xl mb-2 flex-row">
                    <Text className="text-xs font-extrabold text-sky-400 mr-2">{idx + 1}.</Text>
                    <View className="flex-1">
                      <BionicText className="text-xs text-slate-100 mb-1 leading-snug">
                        {item.task}
                      </BionicText>
                      <Text className="text-[10px] text-slate-400">
                        {item.durationMins} mins • {item.rationale}
                      </Text>
                    </View>
                  </View>
                ))}

                <Text className="text-[11px] text-emerald-400/90 mt-1 italic">
                  💡 Neuro-Rule: Do ONLY step 1 right now. You are permitted to ignore the rest of the universe.
                </Text>
              </View>
            )}
          </View>
        </View>
      </Modal>
    </>
  );
};

export default PanicButton;
