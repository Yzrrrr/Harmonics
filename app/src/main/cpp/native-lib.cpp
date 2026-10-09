#include <jni.h>
#include <string>
#include "fluidsynth/include/fluidsynth.h"
#include "fluidsynth/include/fluidsynth/midi.h"
#include "fluidsynth/include/fluidsynth/event.h"
#include <android/log.h>
#include <vector>
#include <chrono>
#include <__thread/this_thread.h>
#include <thread>
#include <condition_variable>
#include <map>

#define LOG_TAG "FluidSynthTest"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

fluid_settings_t *settings = nullptr;
fluid_synth_t *synth = nullptr;
fluid_audio_driver_t *adriver = nullptr;

int preset_number = 25; // 鼓组设置
std::mutex mtx;
std::condition_variable cv;
std::string fileNameStr;
std::string filePathStr;


fluid_sequencer_t *seq;

short synth_id,mySeqID;
short recorder_id;
int BPM = 120;
int bar = 4;
int clap = 4;
int tempBPM = BPM;
int tempbar = bar;
int tempclap = clap;
int loopLengthMs = (bar * clap * 60000) / BPM;
int count = 0; // 节拍器计数
unsigned int startTime;

// 时间的编码： 在loop中，loopLengthMs是整个loop循环的实际时长，通过bar、clap、BPM计算得到。在存储回放、鼓机、和弦时间时采用一下编码方法：最小单位对应16分音符（即拍子时长的四分之一），总长度为bar * clap * 4。

bool isRecording = false;
bool isPlaying = false;
bool ifMetronomeON = false;
bool ifSaving = false;


struct MidiEvent {
    unsigned int time;
    int note;
    int velocity;
    bool isNoteOn;
};

std::vector<MidiEvent> midiTrack;
std::map<std::pair<int, int>, std::vector<MidiEvent>> DrumMidiTrack;
std::map<std::pair<int, int>, std::vector<MidiEvent>> ChordMidiTrack;

void reCreateFluidSynthForSave();

extern "C"
void JNIEXPORT JNICALL Java_com_example_project2_FluidSynthManager_destoryFluidSynth(JNIEnv *env, jobject thiz);

extern "C"
JNIEXPORT void JNICALL
Java_com_example_project2_FluidSynthManager_setBasicMusicInfo(JNIEnv *env, jobject /* this */, jint jBPM, jint jbar, jint jclap) {
    // 使用temp暂存，直到下一周期设置
    tempBPM = jBPM;
    tempbar = jbar;
    tempclap = jclap;
    __android_log_print(ANDROID_LOG_DEBUG, "BasicMusicInfo", "reset temp BPM %d, clap %d, bar %d", tempBPM, tempclap , tempbar);
}

void midi_record_callback(unsigned int time, int note, int velocity, bool isNoteOn) {
    // 记录 MIDI 事件
    if (isRecording) {
        midiTrack.push_back({time, note, velocity, isNoteOn});
        __android_log_print(ANDROID_LOG_DEBUG, "FluidSynth", "Recorded MIDI Event: Time=%d, Note=%d, Vel=%d, On=%d",
                            time, note, velocity, isNoteOn);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_project2_FluidSynthManager_setDrumNote(JNIEnv *env, jobject /* this */, jint note, jint timeNum, jint svel) {
    unsigned int time = (timeNum * 60000) / (BPM * 4);

    DrumMidiTrack[{timeNum, note}].push_back({time, note, svel, true});
    DrumMidiTrack[{timeNum, note}].push_back({time + (timeNum * 60000) / (BPM * 4), note, svel, false});
    __android_log_print(ANDROID_LOG_DEBUG, "FluidSynth", "Recorded drum MIDI Event: Time=%d, Note=%d, Vel=%d, On=%d",
                        time, note, svel, true);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_project2_FluidSynthManager_delDrumNote(JNIEnv *env, jobject /* this */, jint note , jint timeNum) {
    auto key = std::make_pair(timeNum, note);
    auto it = DrumMidiTrack.find(key);
    if (it != DrumMidiTrack.end()) {
        DrumMidiTrack.erase(it);
        __android_log_print(ANDROID_LOG_DEBUG, "FluidSynth", "Delete drum MIDI Event: Time=%d, Note=%d, On=%d",
                            timeNum, note, true);
    } else {
        __android_log_print(ANDROID_LOG_DEBUG, "FluidSynth", "Not found drum MIDI Event: Time=%d, Note=%d, On=%d",
                            timeNum, note, true);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_project2_FluidSynthManager_setChordNote(JNIEnv *env, jobject /* this */, jint note, jint timeNum, jint svel, jint clapOnCount) {
    unsigned int time = (timeNum * 60000) / (BPM * 4) ;

    ChordMidiTrack[{timeNum, note}].push_back({time, note, svel, true});
    ChordMidiTrack[{timeNum, note}].push_back({time+(timeNum * 60000) / (BPM * 4), note, svel, false});
    __android_log_print(ANDROID_LOG_DEBUG, "ChordsDebug", "Recorded chord MIDI Event: Time=%d, Note=%d, Vel=%d, On=%d",
                        time, note, svel, true);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_project2_FluidSynthManager_delChordNote(JNIEnv *env, jobject /* this */, jint note , jint timeNum) {
    auto key = std::make_pair(timeNum, note);
    auto it = ChordMidiTrack.find(key);
    if (it != ChordMidiTrack.end()) {
        ChordMidiTrack.erase(it);
        __android_log_print(ANDROID_LOG_DEBUG, "ChordsDebug", "Delete chord MIDI Event: Time=%d, Note=%d, On=%d",
                            timeNum, note, true);
    } else {
        __android_log_print(ANDROID_LOG_DEBUG, "ChordsDebug", "Not found chord MIDI Event: Time=%d, Note=%d, On=%d",
                            timeNum, note, true);
    }
}

std::mutex destroyMutex;
std::condition_variable destroyCond;
std::atomic<bool> shouldDestroyFluidSynth(false);

extern "C"
JNIEXPORT void JNICALL
Java_com_example_project2_FluidSynthManager_delAllChordNote(JNIEnv *env, jobject /* this */) {
    ChordMidiTrack.clear();
    __android_log_print(ANDROID_LOG_DEBUG, "ChordsDebug", "Delete all chord MIDI Event");
}
bool OnSetting = false;

void StopSaveWav() {
    if (!ifSaving) return;

    // 停止录制和播放
    isRecording = false;
    isPlaying = false;
    ifMetronomeON = false;
    ifSaving = false;

    __android_log_print(ANDROID_LOG_DEBUG, "SaverDebug", "Stopping WAV save process...");

    if (synth) {
        fluid_synth_all_sounds_off(synth, -1);  // 关闭所有声音
        fluid_synth_all_notes_off(synth, -1);   // 停止所有音符
    }
    {
        std::lock_guard<std::mutex> lock(destroyMutex);
        shouldDestroyFluidSynth = true;
    }
    destroyCond.notify_one();
    __android_log_print(ANDROID_LOG_INFO, "SaverDebug", "WAV file saved and FluidSynth stopped.");
}

void SaveWav(){
    if(ifSaving) {
        if (count == 0 && !OnSetting) {
            __android_log_print(ANDROID_LOG_DEBUG, "SaverDebug",
                                "Starting WAV save process...");
            if (seq) {
                fluid_sequencer_remove_events(seq, -1, synth_id, -1 );  // 清空所有未播放的事件
            }
            if (!adriver) {
                __android_log_print(ANDROID_LOG_ERROR, "SaverDebug", "Failed to create adriver.");
                return;
            } else{
                __android_log_print(ANDROID_LOG_DEBUG, "SaverDebug", "adriver OK");
            }

            isRecording = false;
            isPlaying = true;
            ifMetronomeON = false;
            OnSetting = true;
        }else if(count == 0 && OnSetting && !shouldDestroyFluidSynth){
            StopSaveWav();
        }
    }
}


std::string jstringToString(JNIEnv *env, jstring jStr) {
    if (!jStr) return "";

    const char *charStr = env->GetStringUTFChars(jStr, nullptr);
    std::string str(charStr);
    env->ReleaseStringUTFChars(jStr, charStr); // 释放资源

    return str;
}


extern "C"
JNIEXPORT void JNICALL
Java_com_example_project2_FluidSynthManager_destroyFluidSynthLoop(JNIEnv *env, jobject /* this */) {
    while (true) {
        std::unique_lock<std::mutex> lock(destroyMutex);
        destroyCond.wait(lock, []{ return shouldDestroyFluidSynth.load(); }); // 线程睡眠，等待信号
        __android_log_print(ANDROID_LOG_INFO, "destroyFluidSynthLoop", "Waiting for signal");
        if (shouldDestroyFluidSynth) {
            shouldDestroyFluidSynth = false; // 重置标志位
            __android_log_print(ANDROID_LOG_INFO, "destroyFluidSynthLoop", "Destroying FluidSynth in background thread.");
            Java_com_example_project2_FluidSynthManager_destoryFluidSynth(nullptr, nullptr);
            break;
        }
    }
}


extern "C"
JNIEXPORT void JNICALL
Java_com_example_project2_FluidSynthManager_SaveToWav(JNIEnv *env, jobject /* this */, jstring fileName, jstring Path){
    fileNameStr = jstringToString(env, fileName);
    filePathStr = jstringToString(env, Path);
    __android_log_print(ANDROID_LOG_DEBUG, "SaverDebug", "about to save to WAV in %s, %s.",filePathStr.c_str(),fileNameStr.c_str());
    reCreateFluidSynthForSave();
    ifSaving = true;
}

void schedule_note_on(short note, int channel, short velocity, unsigned int delay_ms) {
    fluid_event_t *event = new_fluid_event();
    fluid_event_set_source(event, -1);
    fluid_event_set_dest(event, synth_id);
    fluid_event_noteon(event, channel, note, velocity);
    int sendat = fluid_sequencer_send_at(seq, event, fluid_sequencer_get_tick(seq) + delay_ms, 1);
    __android_log_print(ANDROID_LOG_INFO, "FluidSynth", "fluid_sequencer_send_at return %d note: %d velocity:%d time:%d", sendat, note, velocity,fluid_sequencer_get_tick(seq) + delay_ms);
    delete_fluid_event(event);
}


void schedule_note_off(short note, int channel,unsigned int delay_ms) {
    fluid_event_t *event = new_fluid_event();
    fluid_event_set_source(event, -1);
    fluid_event_set_dest(event, synth_id);
    fluid_event_noteoff(event, channel,note);

    int sendat = fluid_sequencer_send_at(seq, event, fluid_sequencer_get_tick(seq) + delay_ms, 1);
    __android_log_print(ANDROID_LOG_INFO, "FluidSynth", "fluid_sequencer_send_at return %d time:%d", sendat,fluid_sequencer_get_tick(seq) + delay_ms);
    delete_fluid_event(event);
}

void loop_drum_midi_events() {
    for (const auto& [key, events] : DrumMidiTrack) {
        for (const auto& event : events) {
            if (event.isNoteOn) {
                schedule_note_on(event.note, 9, event.velocity, event.time);
            } else {
                schedule_note_off(event.note, 9,   event.time);
            }
        }
    }
}

void loop_chord_midi_events() {
    for (const auto& [key, events] : ChordMidiTrack) {
        for (const auto& event : events) {
            if (event.isNoteOn) {
                schedule_note_on(event.note, 8, event.velocity, event.time);
                __android_log_print(ANDROID_LOG_INFO, "ChordsDebug", "schedule_note_on chord %d on： %d", event.note, event.time);
            } else {
                schedule_note_off(event.note, 8,   event.time);
            }
        }
    }
}

void play_chord(){
    if(count == 0){
        loop_chord_midi_events();
    }
}

void play_drum(){
    if(count % clap == 0){
        loop_drum_midi_events();
    }
}

void playMetronome(){
    if(ifMetronomeON) {
        if (count == 0){
            short note = 80;
            short velocity = 120;
            schedule_note_on(note, 0, velocity, 0);  // 立即触发 Note On
            schedule_note_off(note, 0, 500);
        }else {
            short note = 60;
            short velocity = 100;
            schedule_note_on(note, 0, velocity, 0);  // 立即触发 Note On
            schedule_note_off(note, 0, 500);
        }
    }
}

void loop_midi_events() {
    for (const auto &event : midiTrack) {
        if (event.isNoteOn) {
            schedule_note_on(event.note,1, event.velocity, event.time);
        } else {
            schedule_note_off(event.note,1, event.time);
        }
    }
}

void rescaleMidiEvents(int prevBPM, int newBPM) {
    if (prevBPM == newBPM) return; // BPM 不变，无需调整

    double scaleFactor = (double)prevBPM / newBPM;

    auto rescaleTrack = [&](std::vector<MidiEvent> &track) {
        for (auto &event : track) {
            event.time = static_cast<unsigned int>(event.time * scaleFactor);
        }
    };

    rescaleTrack(midiTrack);

    for (auto &[key, events] : DrumMidiTrack) {
        rescaleTrack(events);
    }

    for (auto &[key, events] : ChordMidiTrack) {
        rescaleTrack(events);
    }

    __android_log_print(ANDROID_LOG_DEBUG, "FluidSynth", "Rescaled MIDI Events: prevBPM=%d, newBPM=%d, scaleFactor=%.4f",
                        prevBPM, newBPM, scaleFactor);
}

void resetBasicinfo(){
    if (count == 0){
        rescaleMidiEvents(BPM,tempBPM);
        BPM = tempBPM;
        clap = tempclap;
        bar = tempbar;
        loopLengthMs = (bar * clap * 60000) / BPM;
        __android_log_print(ANDROID_LOG_DEBUG, "BasicMusicInfo", "reset BPM %d, clap %d, bar %d", BPM, clap, bar);
    }
}

void playBack(){
    if(isPlaying){
        if(count == 0){
            loop_midi_events();
        }
    }
}

// 向前端传输count节拍
extern "C" JNIEXPORT jdouble JNICALL
Java_com_example_project2_FluidSynthManager_getCount(JNIEnv *env, jobject thiz) {
    float progress = (float)(count)/(bar*clap -1);
    return progress;
}


// 定义回调函数, 起到确定时序的作用，每拍触发一次，播放节拍器，每四小节开头写入下一轮音符。可以加入重置时序功能
void timer_callback(unsigned int time, fluid_event_t *event, fluid_sequencer_t *seq, void *data) {

    __android_log_print(ANDROID_LOG_DEBUG, "FluidSynth", "Timer Event Triggered at time %u", time);

    SaveWav();
    resetBasicinfo();
    playMetronome(); // 播放节拍器
    playBack();
    play_drum();
    play_chord();

    count = (count + 1) % (bar*clap);
    // 继续发送下一个定时事件，实现周期性回调
    fluid_event_t *new_event = new_fluid_event();
    fluid_event_set_source(new_event, -1);
    fluid_event_set_dest(new_event, mySeqID);
    fluid_event_timer(new_event, nullptr);

    // 设定下一次回调的时间
    unsigned int beatDurationMs = 60000 / BPM; // 每拍时间
    unsigned int next_time = fluid_sequencer_get_tick(seq) + beatDurationMs;
    //unsigned int next_time = fluid_sequencer_get_tick(seq) + loopLengthMs/(bar*clap); // 每拍回调一次
    fluid_sequencer_send_at(seq, new_event, next_time, 1);

    delete_fluid_event(new_event);
}

//启动定时器，loop循环时序的起始位置设置
void startTimer() {
    if (seq == nullptr) return;

    // 创建一个定时事件
    fluid_event_t *event = new_fluid_event();
    fluid_event_set_source(event, -1);
    fluid_event_set_dest(event, mySeqID);
    fluid_event_timer(event, nullptr);
    startTime = fluid_sequencer_get_tick(seq) + loopLengthMs/(bar*clap); //loop开始事件 当前+一拍
    // 发送定时事件，触发首次回调
    fluid_sequencer_send_at(seq, event, startTime, 1);

    delete_fluid_event(event);
}

void print_option( void *data, const char *name, const char *option){
    __android_log_print(ANDROID_LOG_INFO, "FluidSynth", "Available %s: %s", name, option);
}

void fluidsynth_change(int sfid){
    fluid_synth_program_select(synth, 1,  sfid, 0, 1);
    fluid_synth_program_select(synth, 9,  sfid, 120, preset_number);
    fluid_synth_program_select(synth, 8,  sfid, 0, 27);
}


extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_project2_FluidSynthManager_createFluidSynth(JNIEnv *env, jobject thiz) {
    // 检查是否已经初始化，防止重复创建
    if (synth != nullptr) {
        return env->NewStringUTF("FluidSynth already initialized");
    }

    // 创建 FluidSynth 组件
    settings = new_fluid_settings();
    synth = new_fluid_synth(settings);
    //  加载 SoundFont
    int sfid = fluid_synth_sfload(synth, "/data/data/com.example.project2/files/soundfont.sf2", 1);
    if (sfid == -1) {
        __android_log_print(ANDROID_LOG_ERROR, "FluidSynth", "Failed to load SoundFont");
        return env->NewStringUTF("Failed to load SoundFont");
    }
    fluidsynth_change(sfid);
    //创建音频驱动
    fluid_settings_setstr(settings, "audio.driver", "oboe");
    adriver = new_fluid_audio_driver(settings, synth);
    //创建序列器
    seq = new_fluid_sequencer2(0);
    //绑定序列器客户端
    synth_id = fluid_sequencer_register_fluidsynth(seq, synth);
    mySeqID = fluid_sequencer_register_client(seq, "timer", timer_callback, nullptr);
    __android_log_print(ANDROID_LOG_INFO, "FluidSynth", "FluidSynth initialized successfully");
    fluid_settings_foreach_option(settings, "audio.driver", nullptr, print_option);
    startTimer();
    return env->NewStringUTF("FluidSynth initialized successfully");
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_project2_FluidSynthManager_destoryFluidSynth(JNIEnv *env, jobject thiz) {
    __android_log_print(ANDROID_LOG_INFO, "FluidSynthDestory", "Starting FluidSynth destruction...");

    if (adriver) {
        __android_log_print(ANDROID_LOG_INFO, "FluidSynthDestory", "Deleteing fluid_audio_driver.");
        delete_fluid_audio_driver(adriver);
        adriver = nullptr;
        __android_log_print(ANDROID_LOG_INFO, "FluidSynthDestory", "Deleted fluid_audio_driver.");
    }

    if (seq) {
        fluid_sequencer_unregister_client(seq, synth_id);
        fluid_sequencer_unregister_client(seq, mySeqID);
        fluid_sequencer_remove_events(seq, -1, synth_id, -1);
        fluid_sequencer_remove_events(seq, -1, mySeqID, -1);
        delete_fluid_sequencer(seq);
        seq = nullptr;
        __android_log_print(ANDROID_LOG_INFO, "FluidSynthDestory", "Deleted fluid_sequencer.");
    }

    if (synth) {
        delete_fluid_synth(synth);
        synth = nullptr;
        __android_log_print(ANDROID_LOG_INFO, "FluidSynthDestory", "Deleted fluid_synth.");
    }

    if (settings) {
        delete_fluid_settings(settings);
        settings = nullptr;
        __android_log_print(ANDROID_LOG_INFO, "FluidSynthDestory", "Deleted fluid_settings.");
    }

    __android_log_print(ANDROID_LOG_INFO, "FluidSynthDestory", "FluidSynth successfully destroyed.");
}

void reCreateFluidSynthForSave(){
    __android_log_print(ANDROID_LOG_INFO, "SaverDebug", "reCreate FluidSynth For Save.");
    Java_com_example_project2_FluidSynthManager_destoryFluidSynth(nullptr, nullptr);

    // 创建 FluidSynth 组件
    settings = new_fluid_settings();
    //  加载 SoundFont
    synth = new_fluid_synth(settings);
    int sfid = fluid_synth_sfload(synth, "/data/data/com.example.project2/files/soundfont.sf2", 1);
    if (sfid == -1) {
        __android_log_print(ANDROID_LOG_ERROR, "SaverDebug", "Failed to load SoundFont");
        return;
    }

    std::string filePath = filePathStr + "/" + fileNameStr + ".wav";
    const char *filename = filePath.c_str();

    fluid_settings_setstr(settings, "audio.driver", "file");
    fluid_settings_setstr(settings, "audio.file.name", filename);
    fluid_settings_setstr(settings, "audio.file.type", "wav");

    fluidsynth_change(sfid);
    adriver = new_fluid_audio_driver(settings, synth);

    seq = new_fluid_sequencer2(0);
    synth_id = fluid_sequencer_register_fluidsynth(seq, synth);
    //recorder_id = fluid_sequencer_register_client(seq, "MIDI Recorder", midi_record_callback, nullptr);
    mySeqID = fluid_sequencer_register_client(seq, "timer", timer_callback, nullptr);
    //synth_id = fluid_sequencer_register_client(seq, "MIDI Handler", midi_event_callback, synth);
    __android_log_print(ANDROID_LOG_INFO, "SaverDebug", "FluidSynth initialized successfully");
    fluid_settings_foreach_option(settings, "audio.driver", nullptr, print_option);

    count = 0;
    startTimer();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_project2_FluidSynthManager_playNote(JNIEnv *env, jobject /* this */, jint note, jint svel , jint channel) {
    auto snote = (short)note;
    auto vel = (short)svel;
    if (synth != nullptr) {
        schedule_note_on(snote, channel,vel, 0);
        __android_log_print(ANDROID_LOG_INFO, "FluidSynth", "playNote");
        if(isRecording){
            unsigned int time = (fluid_sequencer_get_tick(seq)-startTime) % loopLengthMs;
            midi_record_callback(time, snote, vel, true);
        }
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_project2_FluidSynthManager_stopNote(JNIEnv *env, jobject /* this */, jint note, jint channel) {
    auto snote = (short)note;
    if (synth != nullptr) {
        schedule_note_off(snote, channel, 0);
        __android_log_print(ANDROID_LOG_INFO, "FluidSynth", "stopNote");
        if(isRecording){
            unsigned int time = (fluid_sequencer_get_tick(seq)-startTime) % loopLengthMs;
            midi_record_callback(time, snote,0, false);
        }
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_project2_FluidSynthManager_stopNoteDelay(JNIEnv *env, jobject /* this */, jint note, jint channel) {
    auto snote = (short)note;
    if (synth != nullptr) {
        schedule_note_off(snote,1, 50);
        __android_log_print(ANDROID_LOG_INFO, "FluidSynth", "stopNote");
    }
}

//开启节拍器
extern "C"
JNIEXPORT void JNICALL
Java_com_example_project2_FluidSynthManager_turnMetronomeON(JNIEnv *env, jobject) {
    ifMetronomeON = true;
    __android_log_print(ANDROID_LOG_DEBUG, "FluidSynth", "turnMetronomeOn");
}

//关闭节拍器
extern "C"
JNIEXPORT void JNICALL
Java_com_example_project2_FluidSynthManager_turnMetronomeOff(JNIEnv *env, jobject) {
    ifMetronomeON = false;
    __android_log_print(ANDROID_LOG_DEBUG, "FluidSynth", "turnMetronomeOff");
}

//开始录制
extern "C"
JNIEXPORT void JNICALL
Java_com_example_project2_FluidSynthManager_startRecording(JNIEnv *env, jobject) {
    isRecording = true;
    __android_log_print(ANDROID_LOG_DEBUG, "FluidSynth", "StartRecording");
}
//停止录制
extern "C"
JNIEXPORT void JNICALL
Java_com_example_project2_FluidSynthManager_stopRecording(JNIEnv *env, jobject) {
    isRecording = false;
    __android_log_print(ANDROID_LOG_DEBUG, "FluidSynth", "StopRecording");

}
//停止回放
extern "C"
JNIEXPORT void JNICALL
Java_com_example_project2_FluidSynthManager_stopPlayback(JNIEnv *env, jobject) {
    {
        std::lock_guard<std::mutex> lock(mtx);
        isPlaying = false;
    }
    __android_log_print(ANDROID_LOG_DEBUG, "FluidSynth", "stopPlayback, isPlaying=%d", isPlaying);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_project2_FluidSynthManager_startPlayback(JNIEnv *env, jobject) {
    {
        std::lock_guard<std::mutex> lock(mtx);
        isPlaying = true;
    }
    cv.notify_one();  // 唤醒等待的线程
    __android_log_print(ANDROID_LOG_DEBUG, "FluidSynth", "startPlayback, isPlaying=%d", isPlaying);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_project2_FluidSynthManager_startOverdub(JNIEnv *env, jobject) {
    isPlaying = true;
    isRecording = true;
    __android_log_print(ANDROID_LOG_DEBUG, "FluidSynth", "startOverdub, isPlaying=%d, isRecording=%d", isPlaying, isRecording);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_project2_FluidSynthManager_clearLoop(JNIEnv *env, jobject) {
    midiTrack.clear();
    __android_log_print(ANDROID_LOG_DEBUG, "FluidSynth", "clearLoop");
}



// 换音色：GM program number，bank 不动。键盘在 1 通道，和弦在 8 通道。
extern "C" JNIEXPORT void JNICALL
Java_com_example_project2_FluidSynthManager_setProgram(JNIEnv *env, jobject /* this */, jint channel, jint program) {
    if (synth != nullptr) {
        fluid_synth_program_change(synth, channel, program);
    }
}
