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
import { MockInterviewStreamChunk } from '../services/gemini';

export interface MockPanelProps {
  problemTitle: string;
  problemDescription: string;
  initialCodeSnippet?: string;
  onRunEvaluation: (code: string, language: string) => Promise<void>;
  streamingFeedText: string;
  lastMetrics?: MockInterviewStreamChunk['metrics'];
  isEvaluating: boolean;
  className?: string;
}

export const MockPanel: React.FC<MockPanelProps> = ({
  problemTitle,
  problemDescription,
  initialCodeSnippet = `class Solution {\n  fun twoSum(nums: IntArray, target: Int): IntArray {\n    val map = HashMap<Int, Int>()\n    for (i in nums.indices) {\n      val comp = target - nums[i]\n      if (map.containsKey(comp)) return intArrayOf(map[comp]!, i)\n      map[nums[i]] = i\n    }\n    return intArrayOf()\n  }\n}`,
  onRunEvaluation,
  streamingFeedText,
  lastMetrics,
  isEvaluating,
  className = '',
}) => {
  const [selectedLanguage, setSelectedLanguage] = useState<'Kotlin' | 'Java' | 'Python' | 'C++' | 'Go'>('Kotlin');
  const [code, setCode] = useState(initialCodeSnippet);

  const languages: Array<'Kotlin' | 'Java' | 'Python' | 'C++' | 'Go'> = ['Kotlin', 'Java', 'Python', 'C++', 'Go'];

  return (
    <View className={`flex-1 bg-slate-950 p-4 ${className}`}>
      {/* Problem Specification Card */}
      <View className="p-3.5 bg-slate-900 border border-slate-800 rounded-xl mb-3">
        <View className="flex-row items-center justify-between mb-1.5">
          <Text className="text-xs font-bold text-sky-400">FAANG TECHNICAL ROUND</Text>
          <View className="bg-emerald-950/80 px-2 py-0.5 rounded border border-emerald-500/40">
            <Text className="text-[10px] font-bold text-emerald-400">ONLINE</Text>
          </View>
        </View>
        <Text className="text-base font-bold text-white mb-1">{problemTitle}</Text>
        <BionicText className="text-xs text-slate-300 leading-relaxed">
          {problemDescription}
        </BionicText>
      </View>

      {/* Language Selector */}
      <View className="flex-row mb-2">
        <ScrollView horizontal showsHorizontalScrollIndicator={false}>
          {languages.map((lang) => (
            <TouchableOpacity
              key={lang}
              onPress={() => setSelectedLanguage(lang)}
              className={`px-3 py-1.5 rounded-lg mr-2 border ${
                selectedLanguage === lang ? 'bg-sky-600 border-sky-400' : 'bg-slate-900 border-slate-800'
              }`}
            >
              <Text className="text-xs font-semibold text-white">{lang}</Text>
            </TouchableOpacity>
          ))}
        </ScrollView>
      </View>

      {/* Split-Screen Code Snippet Input */}
      <View className="bg-slate-900/90 border border-slate-800 rounded-xl p-3 mb-3">
        <View className="flex-row items-center justify-between mb-2 pb-2 border-b border-slate-800">
          <Text className="text-xs font-mono text-slate-400">editor.{selectedLanguage.toLowerCase()}</Text>
          <View className="flex-row space-x-1.5">
            <View className="w-2.5 h-2.5 rounded-full bg-rose-500" />
            <View className="w-2.5 h-2.5 rounded-full bg-amber-500" />
            <View className="w-2.5 h-2.5 rounded-full bg-emerald-500" />
          </View>
        </View>

        <TextInput
          multiline
          value={code}
          onChangeText={setCode}
          className="font-mono text-xs text-slate-200 h-44"
          placeholder="Type optimal algorithm implementation here..."
          placeholderTextColor="#64748B"
          autoCapitalize="none"
          autoCorrect={false}
        />
      </View>

      {/* Action Button */}
      <TouchableOpacity
        onPress={() => onRunEvaluation(code, selectedLanguage)}
        disabled={isEvaluating}
        className={`w-full py-3 rounded-xl items-center justify-center mb-3 ${
          isEvaluating ? 'bg-sky-900' : 'bg-sky-600 active:bg-sky-500'
        }`}
      >
        {isEvaluating ? (
          <View className="flex-row items-center">
            <ActivityIndicator size="small" color="#FFFFFF" />
            <Text className="text-white font-bold text-xs ml-2">Staff Engineer Evaluating...</Text>
          </View>
        ) : (
          <Text className="text-white font-bold text-xs">Run Interview Evaluation (Gemini 2.5 Pro)</Text>
        )}
      </TouchableOpacity>

      {/* Real-Time Streaming Logs & Metrics Output */}
      <ScrollView className="flex-1 bg-slate-900 border border-slate-800 rounded-xl p-3">
        <Text className="text-[11px] font-bold text-slate-400 mb-1">LIVE FEEDBACK STREAM:</Text>
        <Text className="text-xs font-mono text-slate-300 mb-3 leading-relaxed">
          {streamingFeedText || 'Waiting for submission to begin evaluation stream...'}
        </Text>

        {lastMetrics && (
          <View className="pt-2 border-t border-slate-800">
            <Text className="text-xs font-bold text-sky-400 mb-2">
              VERDICT: {lastMetrics.seniorVerdict}
            </Text>
            <View className="flex-row justify-between mb-1">
              <Text className="text-[11px] text-slate-400">Logic Correctness:</Text>
              <Text className="text-[11px] font-bold text-emerald-400">{lastMetrics.logicCorrectnessScore}/100</Text>
            </View>
            <View className="flex-row justify-between mb-1">
              <Text className="text-[11px] text-slate-400">Code Quality & Idiom:</Text>
              <Text className="text-[11px] font-bold text-purple-400">{lastMetrics.codeQualityScore}/100</Text>
            </View>
            <View className="flex-row justify-between">
              <Text className="text-[11px] text-slate-400">Delivery Confidence:</Text>
              <Text className="text-[11px] font-bold text-sky-400">{lastMetrics.confidenceScore}/100</Text>
            </View>
          </View>
        )}
      </ScrollView>
    </View>
  );
};

export default MockPanel;
