import { defineStore } from 'pinia'
import { ref, computed, nextTick } from 'vue'
import { songApi, meApi } from '../api'

export const usePlayerStore = defineStore('player', () => {
  const currentSong = ref(null)
  const playlist = ref([])
  const currentIndex = ref(0)
  const isPlaying = ref(false)
  const progress = ref(0)
  const duration = ref(0)
  const volume = ref(80)
  const playMode = ref('sequence') // sequence | loop | random

  const currentTime = ref(0)
  const audioEl = ref(null)

  const hasNext = computed(() => currentIndex.value < playlist.value.length - 1)
  const hasPrev = computed(() => currentIndex.value > 0)

  function loadSong(song) {
    currentSong.value = song
    isPlaying.value = true
    // 将歌曲加入播放列表（如不存在），并更新 currentIndex，使 prev/next/播放模式生效
    if (song?.songId) {
      songApi.play(song.songId).catch(() => {})
      meApi.recordHistory(song.songId).catch(() => {})
      const existingIdx = playlist.value.findIndex(s => s.songId === song.songId)
      if (existingIdx >= 0) {
        currentIndex.value = existingIdx
      } else {
        playlist.value.push(song)
        currentIndex.value = playlist.value.length - 1
      }
    }
    nextTick(() => audioEl.value?.play())
  }

  // 加载整个列表并播放第一首（用于"播放全部"）
  function loadPlaylist(songs, startIndex = 0) {
    if (!Array.isArray(songs) || !songs.length) return
    playlist.value = [...songs]
    currentIndex.value = startIndex
    const first = songs[startIndex]
    currentSong.value = first
    isPlaying.value = true
    if (first?.songId) {
      songApi.play(first.songId).catch(() => {})
      meApi.recordHistory(first.songId).catch(() => {})
    }
    nextTick(() => audioEl.value?.play())
  }

  function play(index) {
    if (index >= 0 && index < playlist.value.length) {
      currentIndex.value = index
      loadSong(playlist.value[index])
    }
  }

  function togglePlay() {
    isPlaying.value = !isPlaying.value
    if (isPlaying.value) audioEl.value?.play()
    else audioEl.value?.pause()
  }

  function next() {
    if (!playlist.value.length) return
    if (playMode.value === 'random') {
      const idx = Math.floor(Math.random() * playlist.value.length)
      play(idx)
    } else if (playMode.value === 'loop') {
      // 单曲循环：重新播放当前歌曲
      if (audioEl.value) {
        audioEl.value.currentTime = 0
        audioEl.value.play()
      } else {
        play(currentIndex.value)
      }
    } else if (hasNext.value) {
      play(currentIndex.value + 1)
    } else if (playMode.value === 'sequence') {
      // 顺序播放到末尾：停止
      isPlaying.value = false
    }
  }

  function prev() {
    if (hasPrev.value) play(currentIndex.value - 1)
  }

  function setAudio(el) {
    audioEl.value = el
    el.addEventListener('timeupdate', () => {
      currentTime.value = el.currentTime
      progress.value = (el.currentTime / el.duration) * 100 || 0
    })
    el.addEventListener('loadedmetadata', () => {
      duration.value = el.duration
    })
    el.addEventListener('ended', () => next())
  }

  return { currentSong, playlist, currentIndex, isPlaying, progress, duration, volume, playMode, currentTime, audioEl, loadSong, play, togglePlay, next, prev, setAudio, hasNext, hasPrev }
})
