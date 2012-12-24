package com.github.axet.play;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

import com.github.axet.play.vlc.LibVlc;
import com.github.axet.play.vlc.Memfile;
import com.github.axet.play.vlc.MemoryFile;
import com.github.axet.play.vlc.MemoryStream;
import com.github.axet.play.vlc.libvlc_callback_t;
import com.github.axet.play.vlc.libvlc_event_manager_t;
import com.github.axet.play.vlc.libvlc_event_type_t;
import com.github.axet.play.vlc.libvlc_media_t;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class PlaySound {

    public interface Listener {
        /**
         * start playing
         */
        public void start();

        /**
         * end reached
         */
        public void stop();

        /**
         * position changed
         * 
         * @param pos
         */
        public void position(float pos);
    }

    ArrayList<Listener> listeners = new ArrayList<PlaySound.Listener>();

    Memfile mem;

    VLC vlc;

    VLCMediaPlayer m;

    libvlc_callback_t evets = new libvlc_callback_t() {
        @Override
        public void libvlc_callback(IntByReference p_event, Pointer p_user_data) {
            switch (p_event.getValue()) {
            case libvlc_event_type_t.libvlc_MediaPlayerEndReached:
                for (Listener l : listeners) {
                    l.position(1.0f);
                    l.stop();
                }
                stop();
                break;
            case libvlc_event_type_t.libvlc_MediaPlayerPositionChanged:
                for (Listener l : listeners) {
                    float pos = LibVlc.INSTANCE.libvlc_media_player_get_position(m.getInstance());
                    l.position(pos);
                }
                break;
            default:
                break;
            }
        }
    };

    public PlaySound() {
    }

    public void addListener(Listener l) {
        listeners.add(l);
    }

    public void removeListener(Listener l) {
        listeners.remove(l);
    }

    void create() {
        vlc = new VLC();

        m = new VLCMediaPlayer();

        String mrl = "memfile://" + mem.getOpen() + "/" + mem.getClose() + "/" + mem.getSize() + "/" + mem.getSeek()
                + "/" + mem.getRead();
        libvlc_media_t fl = LibVlc.INSTANCE.libvlc_media_new_location(vlc.getInstance(), mrl);

        LibVlc.INSTANCE.libvlc_media_player_set_media(m.getInstance(), fl);

        libvlc_event_manager_t ev = LibVlc.INSTANCE.libvlc_media_player_event_manager(m.getInstance());
        LibVlc.INSTANCE.libvlc_event_attach(ev, libvlc_event_type_t.libvlc_MediaPlayerEndReached, evets, null);
        LibVlc.INSTANCE.libvlc_event_attach(ev, libvlc_event_type_t.libvlc_MediaPlayerPositionChanged, evets, null);
    }

    public void open(File f) {
        mem = new MemoryFile(f);

        create();
    }

    public void open(InputStream is) {

        mem = new MemoryStream(is);

        create();
    }

    public void play() {
        setVolume(100);
        LibVlc.INSTANCE.libvlc_media_player_play(m.getInstance());
    }

    public void stop() {
        LibVlc.INSTANCE.libvlc_audio_set_volume(m.getInstance(), 0);
        LibVlc.INSTANCE.libvlc_media_player_stop(m.getInstance());
    }

    protected void finalize() throws Throwable {
        close();
    }

    public void close() {
        if (m != null) {
            m.close();
            m = null;
        }
        if (vlc != null) {
            vlc.close();
            vlc = null;
        }
    }

    public void setVolume(int v) {
        LibVlc.INSTANCE.libvlc_audio_set_volume(m.getInstance(), v);
    }
}
