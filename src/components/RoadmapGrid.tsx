/**
 * CareerForge - Career Acceleration Platform for BTech Engineering Students
 * Copyright (C) 2026 CareerForge Contributors
 *
 * Licensed under the GNU Affero General Public License v3.0 or later (AGPL-3.0-or-later).
 * See LICENSE file in the project root for full license information.
 */

import React, { useState, useMemo } from 'react';
import { View, Text, TouchableOpacity, ScrollView } from 'react-native';
import { SkillRoadmapsDocType } from '../db/database';
import { BionicText } from './BionicText';

export interface RoadmapGridProps {
  nodes: SkillRoadmapsDocType[];
  onToggleStatus: (nodeId: string, currentStatus: string) => void;
  className?: string;
}

export const RoadmapGrid: React.FC<RoadmapGridProps> = ({
  nodes,
  onToggleStatus,
  className = '',
}) => {
  const [selectedSemester, setSelectedSemester] = useState<number | null>(null);
  const [showOnlyGaps, setShowOnlyGaps] = useState<boolean>(false);

  const semesters = [1, 2, 3, 4, 5, 6, 7, 8];

  const totalGaps = useMemo(() => nodes.filter((n) => n.isAiGap).length, [nodes]);

  const filteredNodes = useMemo(() => {
    return nodes.filter((node) => {
      const semMatch = selectedSemester === null || node.semester === selectedSemester;
      const gapMatch = !showOnlyGaps || node.isAiGap;
      return semMatch && gapMatch;
    });
  }, [nodes, selectedSemester, showOnlyGaps]);

  return (
    <View className={`flex-1 bg-slate-950 p-4 ${className}`}>
      {/* Semester Tabs Carousel */}
      <View className="mb-4">
        <ScrollView horizontal showsHorizontalScrollIndicator={false} className="flex-row">
          <TouchableOpacity
            onPress={() => {
              setSelectedSemester(null);
              setShowOnlyGaps(false);
            }}
            className={`px-3 py-2 rounded-lg mr-2 border ${
              selectedSemester === null && !showOnlyGaps
                ? 'bg-sky-600 border-sky-400'
                : 'bg-slate-900 border-slate-800'
            }`}
          >
            <Text className="text-xs font-bold text-white">All 8 Sems</Text>
          </TouchableOpacity>

          {semesters.map((sem) => {
            const semGapCount = nodes.filter((n) => n.semester === sem && n.isAiGap).length;
            const isSelected = selectedSemester === sem && !showOnlyGaps;

            return (
              <TouchableOpacity
                key={`sem-tab-${sem}`}
                onPress={() => {
                  setSelectedSemester(sem);
                  setShowOnlyGaps(false);
                }}
                className={`flex-row items-center px-3 py-2 rounded-lg mr-2 border ${
                  isSelected ? 'bg-sky-600 border-sky-400' : 'bg-slate-900 border-slate-800'
                }`}
              >
                <Text className="text-xs font-semibold text-white">Sem {sem}</Text>
                {semGapCount > 0 && (
                  <View className="w-2 h-2 rounded-full bg-rose-500 ml-1.5 animate-pulse" />
                )}
              </TouchableOpacity>
            );
          })}

          {totalGaps > 0 && (
            <TouchableOpacity
              onPress={() => setShowOnlyGaps(!showOnlyGaps)}
              className={`flex-row items-center px-3 py-2 rounded-lg border ${
                showOnlyGaps ? 'bg-rose-950 border-rose-500' : 'bg-slate-900 border-slate-800'
              }`}
            >
              <Text className="text-xs font-bold text-rose-400">🚨 AI Gaps ({totalGaps})</Text>
            </TouchableOpacity>
          )}
        </ScrollView>
      </View>

      {/* AI Gap Banner if active */}
      {totalGaps > 0 && (
        <View className="mb-4 p-3 bg-amber-950/40 border border-amber-500/40 rounded-xl flex-row items-center">
          <Text className="text-lg mr-2">⚡</Text>
          <View className="flex-1">
            <Text className="text-xs font-bold text-amber-300">Gemini Graph Gap Analyzer Active</Text>
            <Text className="text-[11px] text-amber-200/80">
              {totalGaps} nodes flagged as missing for Tier 1 placement bar.
            </Text>
          </View>
        </View>
      )}

      {/* Roadmap Milestone Nodes */}
      <ScrollView className="flex-1">
        {filteredNodes.map((node) => {
          const isGap = node.isAiGap;
          const isCompleted = node.status === 'COMPLETED';
          const isInProgress = node.status === 'IN_PROGRESS';

          return (
            <View
              key={node.id}
              className={`p-4 rounded-xl mb-3 border ${
                isGap
                  ? 'bg-rose-950/20 border-rose-600'
                  : isCompleted
                  ? 'bg-slate-900/90 border-emerald-500/60'
                  : isInProgress
                  ? 'bg-slate-900/90 border-sky-500/60'
                  : 'bg-slate-900/50 border-slate-800'
              }`}
            >
              {/* Header: Semester & Category */}
              <View className="flex-row items-center justify-between mb-2">
                <View className="flex-row items-center">
                  <View className="bg-sky-950 px-2 py-0.5 rounded mr-2 border border-sky-600/40">
                    <Text className="text-[10px] font-bold text-sky-400">SEM {node.semester}</Text>
                  </View>
                  <Text className="text-xs text-slate-400 font-medium">
                    {node.subject} • {node.category}
                  </Text>
                </View>

                {/* Status Toggle Pill */}
                <TouchableOpacity
                  onPress={() => onToggleStatus(node.id, node.status)}
                  className={`px-2.5 py-1 rounded-full border ${
                    isCompleted
                      ? 'bg-emerald-950 border-emerald-500'
                      : isInProgress
                      ? 'bg-sky-950 border-sky-500'
                      : 'bg-slate-800 border-slate-700'
                  }`}
                >
                  <Text
                    className={`text-[10px] font-bold ${
                      isCompleted ? 'text-emerald-300' : isInProgress ? 'text-sky-300' : 'text-slate-400'
                    }`}
                  >
                    {isCompleted ? '✓ Done' : isInProgress ? '⏳ In Progress' : '○ Not Started'}
                  </Text>
                </TouchableOpacity>
              </View>

              {/* Title */}
              <Text className="text-base font-bold text-white mb-1.5">{node.title}</Text>

              {/* Description using BionicText */}
              <BionicText className="text-xs leading-relaxed text-slate-300 mb-2">
                {node.description}
              </BionicText>

              {/* AI Gap Indicator Warning Callout */}
              {isGap && (
                <View className="mt-2 p-2.5 bg-rose-950/70 border border-rose-500/60 rounded-lg">
                  <Text className="text-xs font-black text-rose-400 mb-1">
                    ⚠️ AI GAP DETECTED [Priority: {node.gapPriority}]
                  </Text>
                  {node.aiGapReason && (
                    <Text className="text-[11px] text-rose-200 mb-1">
                      Why Demanded: {node.aiGapReason}
                    </Text>
                  )}
                  {node.actionPlan && (
                    <Text className="text-[11px] text-amber-300 font-semibold">
                      Action Plan: {node.actionPlan}
                    </Text>
                  )}
                </View>
              )}
            </View>
          );
        })}
      </ScrollView>
    </View>
  );
};

export default RoadmapGrid;
