/**
 * CareerForge - Career Acceleration Platform for BTech Engineering Students
 * Copyright (C) 2026 CareerForge Contributors
 *
 * Licensed under the GNU Affero General Public License v3.0 or later (AGPL-3.0-or-later).
 * See LICENSE file in the project root for full license information.
 */

import React, { useState, useEffect, useRef } from 'react';
import { View, Text, TouchableOpacity, ScrollView, Animated } from 'react-native';
import { BionicText } from './BionicText';

export interface CodeSnippetChallenge {
  id: string;
  title: string;
  code: string;
  complexity: string; // 'O(1)', 'O(log N)', 'O(N)', 'O(N log N)', 'O(N^2)', 'O(2^N)'
  explanation: string;
}

export const SNIPPET_CHALLENGES: CodeSnippetChallenge[] = [
  {
    id: 'c1',
    title: 'Binary Search Bounds Check',
    code: 'var l = 0, r = n - 1\nwhile (l <= r) {\n  val mid = l + (r - l) / 2\n  if (arr[mid] == target) return mid\n  if (arr[mid] < target) l = mid + 1 else r = mid - 1\n}',
    complexity: 'O(log N)',
    explanation: 'Divides the search space in half at each iteration.',
  },
  {
    id: 'c2',
    title: 'Nested Matrix Traversal',
    code: 'for (i in 0 until n) {\n  for (j in 0 until n) {\n    matrix[i][j] += offset\n  }\n}',
    complexity: 'O(N^2)',
    explanation: 'Quadratic double nested loop over an N x N matrix.',
  },
  {
    id: 'c3',
    title: 'Divide-and-Conquer Merge Sort',
    code: 'fun sort(arr: IntArray) {\n  if (arr.size <= 1) return\n  val mid = arr.size / 2\n  val left = sort(arr.copyOfRange(0, mid))\n  val right = sort(arr.copyOfRange(mid, arr.size))\n  merge(left, right)\n}',
    complexity: 'O(N log N)',
    explanation: 'Recursion tree has depth log N, and merging takes linear O(N) work at each level.',
  },
  {
    id: 'c4',
    title: 'Constant Hash Map Lookup',
    code: 'val user = userMap[userId]\nreturn user?.isActive ?: false',
    complexity: 'O(1)',
    explanation: 'Amortized constant time key hashing and bucket access.',
  },
  {
    id: 'c5',
    title: 'Naive Recursive Fibonacci',
    code: 'fun fib(n: Int): Int {\n  if (n <= 1) return n\n  return fib(n - 1) + fib(n - 2)\n}',
    complexity: 'O(2^N)',
    explanation: 'Binary recursion tree branches into 2 calls per step without memoization.',
  },
  {
    id: 'c6',
    title: 'Linear Two Pointers Scan',
    code: 'var l = 0, r = arr.size - 1\nwhile (l < r) {\n  val sum = arr[l] + arr[r]\n  if (sum == target) return intArrayOf(l, r)\n  if (sum < target) l++ else r--\n}',
    complexity: 'O(N)',
    explanation: 'Each pointer advances at most N steps total across the array.',
  },
];

export interface BigOAgilityGameProps {
  onGameComplete?: (finalScore: number, accuracy: number, maxCombo: number) => void;
  className?: string;
}

export const BigOAgilityGame: React.FC<BigOAgilityGameProps> = ({
  onGameComplete,
  className = '',
}) => {
  const [isPlaying, setIsPlaying] = useState(false);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [score, setScore] = useState(0);
  const [combo, setCombo] = useState(0);
  const [maxCombo, setMaxCombo] = useState(0);
  const [timeLeft, setTimeLeft] = useState(30);
  const [feedback, setFeedback] = useState<{ isCorrect: boolean; text: string } | null>(null);

  const pulseAnim = useRef(new Animated.Value(1)).current;

  const complexityOptions = ['O(1)', 'O(log N)', 'O(N)', 'O(N log N)', 'O(N^2)', 'O(2^N)'];

  useEffect(() => {
    let timer: NodeJS.Timeout;
    if (isPlaying && timeLeft > 0) {
      timer = setInterval(() => setTimeLeft((t) => t - 1), 1000);
    } else if (isPlaying && timeLeft === 0) {
      setIsPlaying(false);
      onGameComplete?.(score, Math.round((score / (currentIndex * 100 || 1)) * 100), maxCombo);
    }
    return () => clearInterval(timer);
  }, [isPlaying, timeLeft]);

  const startGame = () => {
    setIsPlaying(true);
    setScore(0);
    setCombo(0);
    setMaxCombo(0);
    setTimeLeft(30);
    setCurrentIndex(0);
    setFeedback(null);
  };

  const handleSelectComplexity = (selected: string) => {
    if (!isPlaying) return;

    const currentChallenge = SNIPPET_CHALLENGES[currentIndex % SNIPPET_CHALLENGES.length];
    const isCorrect = selected === currentChallenge.complexity;

    Animated.sequence([
      Animated.timing(pulseAnim, { toValue: 1.05, duration: 80, useNativeDriver: true }),
      Animated.timing(pulseAnim, { toValue: 1, duration: 80, useNativeDriver: true }),
    ]).start();

    if (isCorrect) {
      const newCombo = combo + 1;
      const multiplier = Math.min(newCombo, 4);
      const points = 100 * multiplier;
      setScore((s) => s + points);
      setCombo(newCombo);
      setMaxCombo((m) => Math.max(m, newCombo));
      setFeedback({ isCorrect: true, text: `🔥 Correct! +${points} pts (${multiplier}x combo)` });
    } else {
      setCombo(0);
      setFeedback({
        isCorrect: false,
        text: `❌ Actually ${currentChallenge.complexity}: ${currentChallenge.explanation}`,
      });
    }

    // Advance to next snippet
    setTimeout(() => {
      setCurrentIndex((idx) => idx + 1);
      setFeedback(null);
    }, 900);
  };

  const currentChallenge = SNIPPET_CHALLENGES[currentIndex % SNIPPET_CHALLENGES.length];

  return (
    <View className={`bg-slate-950 p-4 rounded-2xl border border-slate-800 ${className}`}>
      {/* Top Header: Score, Time & Combo */}
      <View className="flex-row items-center justify-between mb-3 pb-3 border-b border-slate-800">
        <View>
          <Text className="text-xs font-bold text-sky-400">BIG-O AGILITY RUN</Text>
          <Text className="text-lg font-black text-white">{score} PTS</Text>
        </View>

        {/* Combo Multiplier Badge */}
        <View className="flex-row items-center">
          {combo > 1 && (
            <View className="bg-amber-950 border border-amber-500/80 px-2.5 py-1 rounded-full mr-2">
              <Text className="text-xs font-black text-amber-300">
                {Math.min(combo, 4)}x COMBO 🔥
              </Text>
            </View>
          )}

          {/* Countdown Clock */}
          <View className="bg-slate-900 border border-slate-700 px-3 py-1.5 rounded-xl">
            <Text
              className={`font-mono text-sm font-extrabold ${
                timeLeft <= 5 ? 'text-rose-500 animate-pulse' : 'text-sky-400'
              }`}
            >
              ⏱ {timeLeft}s
            </Text>
          </View>
        </View>
      </View>

      {!isPlaying ? (
        /* Pre-game Screen */
        <View className="items-center py-6">
          <Text className="text-3xl mb-2">⚡</Text>
          <Text className="text-base font-extrabold text-white text-center mb-1">
            Algorithmic Speed & Asymptotics
          </Text>
          <BionicText className="text-xs text-slate-400 text-center mb-4 leading-relaxed max-w-xs">
            Identify the tightest time complexity bound for rapid OA & technical interview problem elimination.
          </BionicText>

          <TouchableOpacity
            onPress={startGame}
            className="bg-sky-600 active:bg-sky-500 px-6 py-3 rounded-xl shadow-lg shadow-sky-600/30"
          >
            <Text className="text-white font-extrabold text-sm">Start 30s Agility Challenge</Text>
          </TouchableOpacity>
        </View>
      ) : (
        /* Active Game Arena */
        <Animated.View style={{ transform: [{ scale: pulseAnim }] }}>
          {/* Challenge Snippet Card */}
          <View className="bg-slate-900 border border-slate-800 p-3.5 rounded-xl mb-3">
            <Text className="text-xs font-bold text-slate-300 mb-1.5">
              Snippet #{currentIndex + 1}: {currentChallenge.title}
            </Text>
            <View className="bg-slate-950 p-2.5 rounded-lg border border-slate-800/80">
              <Text className="font-mono text-xs text-sky-200 leading-relaxed">
                {currentChallenge.code}
              </Text>
            </View>
          </View>

          {/* Feedback Banner */}
          {feedback && (
            <View
              className={`p-2 rounded-lg mb-3 border ${
                feedback.isCorrect ? 'bg-emerald-950/80 border-emerald-500' : 'bg-rose-950/80 border-rose-500'
              }`}
            >
              <Text
                className={`text-xs font-bold text-center ${
                  feedback.isCorrect ? 'text-emerald-300' : 'text-rose-300'
                }`}
              >
                {feedback.text}
              </Text>
            </View>
          )}

          {/* Quick Choice Buttons Grid */}
          <Text className="text-[11px] font-semibold text-slate-400 mb-2">
            SELECT ASYMPTOTIC RUNTIME:
          </Text>
          <View className="flex-row flex-wrap justify-between">
            {complexityOptions.map((opt) => (
              <TouchableOpacity
                key={opt}
                onPress={() => handleSelectComplexity(opt)}
                className="w-[48%] py-3 mb-2 bg-slate-900 active:bg-sky-900/60 border border-slate-800 active:border-sky-500 rounded-xl items-center justify-center"
              >
                <Text className="font-mono text-sm font-bold text-white">{opt}</Text>
              </TouchableOpacity>
            ))}
          </View>
        </Animated.View>
      )}
    </View>
  );
};

export default BigOAgilityGame;
