/**
 * CareerForge - Career Acceleration Platform for BTech Engineering Students
 * Copyright (C) 2026 CareerForge Contributors
 *
 * Licensed under the GNU Affero General Public License v3.0 or later (AGPL-3.0-or-later).
 * See LICENSE file in the project root for full license information.
 */

import React, { useState, useRef, useEffect } from 'react';
import { View, Text, TextInput, TouchableOpacity, ScrollView, ActivityIndicator } from 'react-native';
import { BionicText } from './BionicText';
import { GeminiOrchestrator } from '../services/gemini';

export interface ChatMessage {
  id: string;
  role: 'user' | 'model';
  text: string;
  timestamp: string;
}

export interface CareerChatbotProps {
  studentSemester?: number;
  targetRole?: string;
  targetTier?: string;
  geminiOrchestrator: GeminiOrchestrator;
  className?: string;
}

export const CareerChatbot: React.FC<CareerChatbotProps> = ({
  studentSemester = 5,
  targetRole = 'Backend Systems & Distributed SDE',
  targetTier = 'Tier 1 Product / Unicorn',
  geminiOrchestrator,
  className = '',
}) => {
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: 'init_1',
      role: 'model',
      text: `Hello! I am your Senior Staff Mentor. I am calibrated for your Sem ${studentSemester} curriculum targeting ${targetRole} at ${targetTier}. What algorithmic bottleneck, system architecture problem, or placement dilemma should we deconstruct?`,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    },
  ]);
  const [inputText, setInputText] = useState('');
  const [isStreaming, setIsStreaming] = useState(false);
  const [currentStreamChunk, setCurrentStreamChunk] = useState('');
  const [enableBionic, setEnableBionic] = useState(false);

  const scrollViewRef = useRef<ScrollView>(null);

  const quickPrompts = [
    'How do I master DP state transitions in 14 days?',
    'Architecture breakdown: Kafka vs RabbitMQ for order pipelines',
    'Cold outreach script for Engineering Managers on LinkedIn',
    'Top 5 edge cases for Two Pointers & Sliding Window',
  ];

  const handleSendMessage = async (textToSend: string) => {
    const trimmed = textToSend.trim();
    if (!trimmed || isStreaming) return;

    const userMsg: ChatMessage = {
      id: `usr_${Date.now()}`,
      role: 'user',
      text: trimmed,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    };

    setMessages((prev) => [...prev, userMsg]);
    setInputText('');
    setIsStreaming(true);
    setCurrentStreamChunk('');

    const memoryContext = `Student is in BTech Semester ${studentSemester}, aiming for ${targetRole} (${targetTier}). Keep responses direct, engineering-grounded, and actionable.`;

    const historyForGemini: Array<{ role: 'user' | 'model'; parts: [{ text: string }] }> = messages.map(
      (m) => ({
        role: m.role,
        parts: [{ text: m.text }],
      })
    );

    try {
      let accumulated = '';
      const streamGenerator = geminiOrchestrator.streamCareerMentorResponse(
        trimmed,
        historyForGemini,
        memoryContext
      );

      for await (const chunk of streamGenerator) {
        accumulated += chunk;
        setCurrentStreamChunk(accumulated);
      }

      setMessages((prev) => [
        ...prev,
        {
          id: `model_${Date.now()}`,
          role: 'model',
          text: accumulated,
          timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        },
      ]);
    } catch (err) {
      console.error('[CareerChatbot] streaming error:', err);
    } finally {
      setIsStreaming(false);
      setCurrentStreamChunk('');
    }
  };

  useEffect(() => {
    scrollViewRef.current?.scrollToEnd({ animated: true });
  }, [messages, currentStreamChunk]);

  return (
    <View className={`flex-1 bg-slate-950 p-4 ${className}`}>
      {/* 1. Chatbot Memory Context Capsule Banner */}
      <View className="bg-slate-900 border border-slate-800 rounded-xl p-3 mb-3">
        <View className="flex-row items-center justify-between mb-1.5">
          <View className="flex-row items-center">
            <Text className="text-base mr-1.5">🧠</Text>
            <Text className="text-xs font-bold text-sky-400">PERSISTENT CHATBOT MEMORY ACTIVE</Text>
          </View>
          <TouchableOpacity
            onPress={() => setEnableBionic(!enableBionic)}
            className={`px-2 py-0.5 rounded border ${
              enableBionic ? 'bg-sky-950 border-sky-400' : 'bg-slate-800 border-slate-700'
            }`}
          >
            <Text className={`text-[10px] font-bold ${enableBionic ? 'text-sky-300' : 'text-slate-400'}`}>
              {enableBionic ? '⚡ Bionic Reading ON' : 'Bionic Reading OFF'}
            </Text>
          </TouchableOpacity>
        </View>

        <View className="flex-row flex-wrap gap-1.5">
          <View className="bg-slate-800 px-2 py-0.5 rounded">
            <Text className="text-[10px] text-slate-300">Sem: {studentSemester}</Text>
          </View>
          <View className="bg-slate-800 px-2 py-0.5 rounded">
            <Text className="text-[10px] text-slate-300">Role: {targetRole}</Text>
          </View>
          <View className="bg-slate-800 px-2 py-0.5 rounded">
            <Text className="text-[10px] text-amber-300">{targetTier}</Text>
          </View>
        </View>
      </View>

      {/* 2. Chat Log Messages */}
      <ScrollView ref={scrollViewRef} className="flex-1 mb-3">
        {messages.map((m) => {
          const isUser = m.role === 'user';

          return (
            <View
              key={m.id}
              className={`mb-3 max-w-[85%] ${isUser ? 'self-end' : 'self-start'}`}
            >
              <View
                className={`p-3 rounded-2xl ${
                  isUser
                    ? 'bg-sky-600 rounded-tr-none'
                    : 'bg-slate-900 border border-slate-800 rounded-tl-none'
                }`}
              >
                {enableBionic ? (
                  <BionicText className="text-xs leading-relaxed text-white">
                    {m.text}
                  </BionicText>
                ) : (
                  <Text className="text-xs leading-relaxed text-white font-sans">
                    {m.text}
                  </Text>
                )}
              </View>
              <Text
                className={`text-[9px] text-slate-500 mt-0.5 ${isUser ? 'text-right' : 'text-left'}`}
              >
                {m.timestamp}
              </Text>
            </View>
          );
        })}

        {/* Live Streaming Response Chunk */}
        {isStreaming && (
          <View className="mb-3 max-w-[85%] self-start">
            <View className="p-3 bg-slate-900 border border-sky-500/60 rounded-2xl rounded-tl-none">
              <Text className="text-xs leading-relaxed text-sky-100 font-sans">
                {currentStreamChunk}
              </Text>
              <View className="flex-row items-center mt-2">
                <ActivityIndicator size="small" color="#38BDF8" />
                <Text className="text-[10px] text-sky-400 font-bold ml-1.5">
                  Streaming Gemini 2.5 Pro...
                </Text>
              </View>
            </View>
          </View>
        )}
      </ScrollView>

      {/* 3. Quick Prompt Suggestions Carousel */}
      <View className="mb-2">
        <ScrollView horizontal showsHorizontalScrollIndicator={false} className="flex-row">
          {quickPrompts.map((prompt, idx) => (
            <TouchableOpacity
              key={`quick-${idx}`}
              onPress={() => handleSendMessage(prompt)}
              className="bg-slate-900 border border-slate-800 px-3 py-1.5 rounded-full mr-2"
            >
              <Text className="text-[11px] text-slate-300">{prompt}</Text>
            </TouchableOpacity>
          ))}
        </ScrollView>
      </View>

      {/* 4. Input Row */}
      <View className="flex-row items-center bg-slate-900 border border-slate-800 rounded-xl px-3 py-1.5">
        <TextInput
          value={inputText}
          onChangeText={setInputText}
          placeholder="Ask engineering mentor about algorithms, systems, or placements..."
          placeholderTextColor="#64748B"
          className="flex-1 text-xs text-white min-h-[38px]"
          onSubmitEditing={() => handleSendMessage(inputText)}
        />
        <TouchableOpacity
          onPress={() => handleSendMessage(inputText)}
          disabled={!inputText.trim() || isStreaming}
          className={`px-3 py-2 rounded-lg ml-2 ${
            inputText.trim() && !isStreaming ? 'bg-sky-600' : 'bg-slate-800'
          }`}
        >
          <Text className="text-white text-xs font-bold">Send</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
};

export default CareerChatbot;
