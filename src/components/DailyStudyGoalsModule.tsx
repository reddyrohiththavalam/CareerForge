/**
 * CareerForge - Career Acceleration Platform for BTech Engineering Students
 * Copyright (C) 2026 CareerForge Contributors
 *
 * Licensed under the GNU Affero General Public License v3.0 or later (AGPL-3.0-or-later).
 * See LICENSE file in the project root for full license information.
 */

import React, { useState, useMemo } from 'react';
import { View, Text, TouchableOpacity, ScrollView } from 'react-native';
import { ParsedTimetableSlot } from '../services/gemini';
import { BionicText } from './BionicText';

export interface DailyStudyGoalsModuleProps {
  slots: ParsedTimetableSlot[];
  onToggleSlotComplete: (slotId: string) => void;
  onResetGoals?: () => void;
  streakDays?: number;
  className?: string;
}

interface CategoryGoal {
  category: string;
  label: string;
  completed: number;
  total: number;
  percentage: number;
  colorClass: string;
  barColor: string;
}

function parseMinutes(timeStr: string): number {
  if (!timeStr) return 0;
  const parts = timeStr.trim().split(':');
  if (parts.length >= 2) {
    const h = parseInt(parts[0], 10) || 0;
    const m = parseInt(parts[1], 10) || 0;
    return h * 60 + m;
  }
  return 0;
}

function getSlotDurationMinutes(slot: ParsedTimetableSlot): number {
  const start = parseMinutes(slot.startTime);
  const end = parseMinutes(slot.endTime);
  return end > start ? end - start : 90;
}

export const DailyStudyGoalsModule: React.FC<DailyStudyGoalsModuleProps> = ({
  slots,
  onToggleSlotComplete,
  onResetGoals,
  streakDays = 14,
  className = '',
}) => {
  const [filter, setFilter] = useState<'ALL' | 'PENDING' | 'COMPLETED' | 'DSA' | 'CORE_CS'>('ALL');

  // Completed slots tracking
  const totalSlots = slots.length;
  const completedSlots = slots.filter((s) => !!s.isCompletedToday).length;
  const overallPercentage = totalSlots > 0 ? Math.round((completedSlots / totalSlots) * 100) : 0;

  // Study hours
  const totalMinutes = slots.reduce((acc, s) => acc + getSlotDurationMinutes(s), 0);
  const completedMinutes = slots
    .filter((s) => !!s.isCompletedToday)
    .reduce((acc, s) => acc + getSlotDurationMinutes(s), 0);
  const totalHours = (totalMinutes / 60).toFixed(1);
  const completedHours = (completedMinutes / 60).toFixed(1);

  // Categorized breakdown
  const categoryBreakdown = useMemo<CategoryGoal[]>(() => {
    const groups: { [key: string]: { label: string; color: string; bar: string } } = {
      DSA: { label: 'Data Structures & Algorithms', color: 'text-sky-400', bar: 'bg-sky-500' },
      CORE_CS: { label: 'Core CS Fundamentals (OS/DBMS)', color: 'text-purple-400', bar: 'bg-purple-500' },
      PROJECT: { label: 'System Design & Projects', color: 'text-emerald-400', bar: 'bg-emerald-500' },
      CONTEST: { label: 'Timed Contests & OAs', color: 'text-amber-400', bar: 'bg-amber-500' },
    };

    return Object.entries(groups).map(([catKey, info]) => {
      const catSlots = slots.filter((s) => (s.activityType || '').toUpperCase() === catKey);
      const total = catSlots.length;
      const completed = catSlots.filter((s) => !!s.isCompletedToday).length;
      const pct = total > 0 ? Math.round((completed / total) * 100) : 0;
      return {
        category: catKey,
        label: info.label,
        completed,
        total,
        percentage: pct,
        colorClass: info.color,
        barColor: info.bar,
      };
    }).filter((g) => g.total > 0);
  }, [slots]);

  // Filtered task items
  const filteredSlots = useMemo(() => {
    switch (filter) {
      case 'PENDING':
        return slots.filter((s) => !s.isCompletedToday);
      case 'COMPLETED':
        return slots.filter((s) => !!s.isCompletedToday);
      case 'DSA':
        return slots.filter((s) => (s.activityType || '').toUpperCase() === 'DSA');
      case 'CORE_CS':
        return slots.filter((s) => (s.activityType || '').toUpperCase() === 'CORE_CS');
      default:
        return slots;
    }
  }, [slots, filter]);

  return (
    <View className={`space-y-4 ${className}`}>
      {/* 1. Daily Study Goals Executive Summary Card */}
      <View className="bg-slate-900 border border-slate-800 rounded-2xl p-4 shadow-xl">
        <View className="flex-row items-center justify-between mb-3">
          <View>
            <View className="flex-row items-center mb-1">
              <View className="bg-sky-500/20 px-2 py-0.5 rounded border border-sky-400/30 mr-2">
                <Text className="text-[10px] font-black text-sky-400">DAILY EXECUTION</Text>
              </View>
              <Text className="text-base font-extrabold text-white">Daily Study Goals</Text>
            </View>
            <Text className="text-xs text-slate-400">
              Track daily engineering milestones and velocity
            </Text>
          </View>

          <View className={`px-3 py-1.5 rounded-lg border ${
            overallPercentage === 100
              ? 'bg-emerald-950 border-emerald-500/50'
              : 'bg-slate-800 border-slate-700'
          }`}>
            <Text className={`text-xs font-black ${
              overallPercentage === 100 ? 'text-emerald-300' : 'text-sky-300'
            }`}>
              {overallPercentage === 100 ? '100% COMPLETE 🎉' : `${overallPercentage}% DONE`}
            </Text>
          </View>
        </View>

        {/* Overall Progress Bar */}
        <View className="mb-3">
          <View className="flex-row justify-between items-center mb-1.5">
            <Text className="text-xs text-slate-300 font-semibold">
              Daily Target Completion
            </Text>
            <Text className="text-xs font-bold text-white">
              {completedSlots} / {totalSlots} Tasks Done
            </Text>
          </View>

          {/* Progress Track */}
          <View className="w-full bg-slate-800 h-3 rounded-full overflow-hidden">
            <View
              className={`h-full rounded-full transition-all duration-300 ${
                overallPercentage === 100
                  ? 'bg-emerald-500'
                  : overallPercentage >= 50
                  ? 'bg-sky-500'
                  : 'bg-amber-500'
              }`}
              style={{ width: `${Math.min(overallPercentage, 100)}%` }}
            />
          </View>
        </View>

        {/* Key Metrics Row */}
        <View className="flex-row justify-between items-center pt-2 border-t border-slate-800/80">
          <View className="flex-row items-center">
            <Text className="text-xs text-slate-400 mr-1">⏱ Focus:</Text>
            <Text className="text-xs font-bold text-slate-200">
              {completedHours} / {totalHours} hrs
            </Text>
          </View>

          <View className="flex-row items-center">
            <Text className="text-xs text-amber-400 mr-1">🔥 Streak:</Text>
            <Text className="text-xs font-bold text-white">
              {streakDays} Days
            </Text>
          </View>

          <View className="flex-row items-center">
            <Text className="text-xs text-emerald-400 mr-1">⚡ Velocity:</Text>
            <Text className="text-xs font-bold text-slate-200">
              {overallPercentage >= 80 ? 'Tier-1 Pace' : 'Accelerating'}
            </Text>
          </View>
        </View>

        {/* Dynamic Tip */}
        <View className="mt-3 bg-sky-950/40 border border-sky-600/30 rounded-xl p-2.5">
          <Text className="text-[11px] text-sky-200 leading-relaxed">
            💡 {overallPercentage === 100
              ? 'Terrific job! All daily engineering blocks completed. Streak multiplier applied.'
              : overallPercentage >= 50
              ? 'Great momentum! Complete remaining problem sets to seal today\'s placement streak.'
              : 'Consistency over intensity: finish your first 90-min engineering block to gain momentum.'}
          </Text>
        </View>
      </View>

      {/* 2. Engineering Tracks Domain Breakdown */}
      {categoryBreakdown.length > 0 && (
        <View className="bg-slate-900 border border-slate-800 rounded-2xl p-4">
          <Text className="text-xs font-black text-slate-400 mb-3 tracking-wider">
            ENGINEERING TRACK PROGRESS
          </Text>

          <View className="space-y-3">
            {categoryBreakdown.map((track) => (
              <View key={track.category} className="space-y-1">
                <View className="flex-row justify-between items-center">
                  <Text className="text-xs font-bold text-slate-200">{track.label}</Text>
                  <View className="flex-row items-center">
                    <Text className="text-xs text-slate-400 mr-2">
                      {track.completed}/{track.total}
                    </Text>
                    <Text className={`text-xs font-black ${track.colorClass}`}>
                      {track.percentage}%
                    </Text>
                  </View>
                </View>

                {/* Track mini progress bar */}
                <View className="w-full bg-slate-800 h-2 rounded-full overflow-hidden">
                  <View
                    className={`h-full rounded-full ${track.barColor}`}
                    style={{ width: `${Math.min(track.percentage, 100)}%` }}
                  />
                </View>
              </View>
            ))}
          </View>
        </View>
      )}

      {/* 3. Filter Chips */}
      <ScrollView horizontal showsHorizontalScrollIndicator={false} className="flex-row space-x-2 py-1">
        {(['ALL', 'PENDING', 'COMPLETED', 'DSA', 'CORE_CS'] as const).map((filterKey) => {
          const isActive = filter === filterKey;
          const count =
            filterKey === 'ALL'
              ? totalSlots
              : filterKey === 'PENDING'
              ? totalSlots - completedSlots
              : filterKey === 'COMPLETED'
              ? completedSlots
              : filterKey === 'DSA'
              ? slots.filter((s) => (s.activityType || '').toUpperCase() === 'DSA').length
              : slots.filter((s) => (s.activityType || '').toUpperCase() === 'CORE_CS').length;

          return (
            <TouchableOpacity
              key={filterKey}
              onPress={() => setFilter(filterKey)}
              className={`px-3 py-1.5 rounded-full border mr-2 ${
                isActive
                  ? 'bg-sky-600 border-sky-400'
                  : 'bg-slate-900 border-slate-800'
              }`}
            >
              <Text className={`text-xs font-bold ${isActive ? 'text-white' : 'text-slate-400'}`}>
                {filterKey} ({count})
              </Text>
            </TouchableOpacity>
          );
        })}
      </ScrollView>

      {/* 4. Actionable Tasks Interactive Checklist */}
      <View className="space-y-2">
        <View className="flex-row justify-between items-center mb-1">
          <Text className="text-xs font-black text-slate-400">
            ENGINEERING TASK LIST ({filteredSlots.length})
          </Text>
          {onResetGoals && completedSlots > 0 && (
            <TouchableOpacity onPress={onResetGoals}>
              <Text className="text-xs font-bold text-sky-400">Reset Today</Text>
            </TouchableOpacity>
          )}
        </View>

        {filteredSlots.length === 0 ? (
          <View className="bg-slate-900/60 border border-slate-800 rounded-xl p-4 items-center">
            <Text className="text-xs text-slate-500">No tasks in this filter view</Text>
          </View>
        ) : (
          filteredSlots.map((slot) => {
            const isCompleted = !!slot.isCompletedToday;
            const duration = getSlotDurationMinutes(slot);
            const durationStr = `${Math.floor(duration / 60)}h ${duration % 60}m`;

            return (
              <View
                key={slot.id}
                className={`p-3 rounded-xl border ${
                  isCompleted
                    ? 'bg-slate-900/80 border-sky-700/50'
                    : 'bg-slate-900/60 border-slate-800'
                }`}
              >
                <View className="flex-row justify-between items-start">
                  <View className="flex-1 mr-2">
                    <View className="flex-row items-center space-x-2 mb-1">
                      <View className="bg-sky-950 px-2 py-0.5 rounded border border-sky-600/40 mr-1.5">
                        <Text className="text-[10px] font-bold text-sky-400">{slot.activityType}</Text>
                      </View>
                      <View className={`px-1.5 py-0.5 rounded mr-1.5 ${
                        slot.priority === 'HIGH' ? 'bg-rose-950/80' : 'bg-slate-800'
                      }`}>
                        <Text className={`text-[9px] font-bold ${
                          slot.priority === 'HIGH' ? 'text-rose-400' : 'text-slate-400'
                        }`}>
                          {slot.priority}
                        </Text>
                      </View>
                      <Text className="text-[10px] text-slate-400">
                        {durationStr}
                      </Text>
                    </View>

                    <Text className={`text-sm font-bold text-white mb-1 ${
                      isCompleted ? 'text-slate-200' : 'text-white'
                    }`}>
                      {slot.topic}
                    </Text>

                    <Text className="text-[11px] text-slate-400 mb-1">
                      {slot.dayOfWeek} • {slot.startTime} - {slot.endTime}
                    </Text>

                    {slot.rationale && (
                      <BionicText className="text-[11px] text-slate-400 leading-relaxed mt-1">
                        {slot.rationale}
                      </BionicText>
                    )}
                  </View>

                  {/* Toggle Checkbox */}
                  <TouchableOpacity
                    onPress={() => onToggleSlotComplete(slot.id)}
                    className={`px-3 py-1.5 rounded-full border items-center justify-center ${
                      isCompleted
                        ? 'bg-emerald-950 border-emerald-500'
                        : 'bg-slate-800 border-slate-700 active:bg-slate-700'
                    }`}
                  >
                    <Text className={`text-[10px] font-bold ${
                      isCompleted ? 'text-emerald-300' : 'text-slate-300'
                    }`}>
                      {isCompleted ? '✓ Done' : '○ Mark Done'}
                    </Text>
                  </TouchableOpacity>
                </View>
              </View>
            );
          })
        )}
      </View>
    </View>
  );
};

export default DailyStudyGoalsModule;
