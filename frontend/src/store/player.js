import { defineStore } from 'pinia'
import { ref, computed, nextTick } from 'vue'
import { songApi } from '../api'

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
    if (playMode.value === 'random') {
      const idx = Math.floor(Math.random() * playlist.value.length)
      play(idx)
    } else if (hasNext.value) {
      play(currentIndex.value + 1)
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
