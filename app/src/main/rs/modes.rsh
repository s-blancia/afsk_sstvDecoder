/*
Copyright 2014 Ahmet Inan <xdsopl@googlemail.com>

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

#ifndef MODES_RSH
#define MODES_RSH

#include "constants.rsh"
#include "state.rsh"
#include "exports.rsh"

static int freerun_reserve(int height) { return (height * 3) / 2; }
void auto_mode(int mode) { automatic_mode_detection = mode; }
void toggle_debug() { debug_mode ^= 1; }

void raw_mode()
{
    blur_power = 0;
    *current_mode = mode_raw;
    current_decoder = decoder_raw;
    freerun_height = maximum_height;
    bitmap_width = maximum_width;
    bitmap_height = maximum_height;
    sync_pulse_detector = init_pulse(2.0f, sync_buildup_ms, sample_rate);
    minimum_length = 0.05f * sample_rate;
    maximum_length = buffer_length;
    scanline_length = maximum_length;
}

void robot36_mode()
{
    blur_power = 2;
    *current_mode = mode_robot36;
    current_decoder = decoder_robot36;
    bitmap_width = 320;
    bitmap_height = 240;
    freerun_height = freerun_reserve(bitmap_height);
    const float tolerance = 0.8f;
    const float sync_ms = 9.0f;
    const float sync_porch_ms = 3.0f;
    const float sep_porch_ms = 1.5f;
    const float y_scan_ms = 88.0f;
    const float uv_scan_ms = 44.0f;
    const float seperator_ms = 4.5f;
    seperator_length = round((seperator_ms * sample_rate) / 1000.0f);
    sync_pulse_detector = init_pulse(tolerance * sync_ms, sync_buildup_ms, sample_rate);

    float y_begin_ms = sync_porch_ms;
    float y_end_ms = y_begin_ms + y_scan_ms;
    float uv_sep_begin_ms = y_end_ms;
    float uv_sep_end_ms = uv_sep_begin_ms + seperator_ms;
    float uv_begin_ms = uv_sep_end_ms + sep_porch_ms;
    float uv_end_ms = uv_begin_ms + uv_scan_ms;

    y_begin = round((y_begin_ms * sample_rate) / 1000.0f);
    y_end = round((y_end_ms * sample_rate) / 1000.0f);
    u_sep_begin = v_sep_begin = round((uv_sep_begin_ms * sample_rate) / 1000.0f);
    u_sep_end = v_sep_end = round((uv_sep_end_ms * sample_rate) / 1000.0f);
    u_begin = v_begin = round((uv_begin_ms * sample_rate) / 1000.0f);
    u_end = v_end = round((uv_end_ms * sample_rate) / 1000.0f);

    scanline_length = robot36_scanline_length;
    minimum_length = (1.0f - scanline_tolerance) * robot36_scanline_length;
    maximum_length = (1.0f + scanline_tolerance) * robot36_scanline_length;
}

void robot72_mode()
{
    blur_power = 3;
    *current_mode = mode_robot72;
    current_decoder = decoder_yuv;
    bitmap_width = 320;
    bitmap_height = 240;
    freerun_height = freerun_reserve(bitmap_height);
    const float tolerance = 0.8f;
    const float sync_ms = 9.0f;
    const float sync_porch_ms = 3.0f;
    const float sep_porch_ms = 1.5f;
    const float y_scan_ms = 138.0f;
    const float uv_scan_ms = 69.0f;
    const float seperator_ms = 4.5f;
    seperator_length = round((seperator_ms * sample_rate) / 1000.0f);
    sync_pulse_detector = init_pulse(tolerance * sync_ms, sync_buildup_ms, sample_rate);

    float y_begin_ms = sync_porch_ms;
    float y_end_ms = y_begin_ms + y_scan_ms;
    float v_sep_begin_ms = y_end_ms;
    float v_sep_end_ms = v_sep_begin_ms + seperator_ms;
    float v_begin_ms = v_sep_end_ms + sep_porch_ms;
    float v_end_ms = v_begin_ms + uv_scan_ms;
    float u_sep_begin_ms = v_end_ms;
    float u_sep_end_ms = u_sep_begin_ms + seperator_ms;
    float u_begin_ms = u_sep_end_ms + sep_porch_ms;
    float u_end_ms = u_begin_ms + uv_scan_ms;

    y_begin = round((y_begin_ms * sample_rate) / 1000.0f);
    y_end = round((y_end_ms * sample_rate) / 1000.0f);
    v_sep_begin = round((v_sep_begin_ms * sample_rate) / 1000.0f);
    v_sep_end = round((v_sep_end_ms * sample_rate) / 1000.0f);
    v_begin = round((v_begin_ms * sample_rate) / 1000.0f);
    v_end = round((v_end_ms * sample_rate) / 1000.0f);
    u_sep_begin = round((u_sep_begin_ms * sample_rate) / 1000.0f);
    u_sep_end = round((u_sep_end_ms * sample_rate) / 1000.0f);
    u_begin = round((u_begin_ms * sample_rate) / 1000.0f);
    u_end = round((u_end_ms * sample_rate) / 1000.0f);

    scanline_length = robot72_scanline_length;
    minimum_length = (1.0f - scanline_tolerance) * robot72_scanline_length;
    maximum_length = (1.0f + scanline_tolerance) * robot72_scanline_length;
}

static void pd_mode(int mode, int scanline, int blur, float yuv_scan_ms, int width, int height)
{
    blur_power = blur;
    *current_mode = mode;
    current_decoder = decoder_pd;
    bitmap_width = width;
    bitmap_height = height;
    freerun_height = freerun_reserve(bitmap_height);
    const float tolerance = 0.8f;
    const float sync_ms = 20.0f;
    const float porch_ms = 2.08f;
    sync_pulse_detector = init_pulse(tolerance * sync_ms, sync_buildup_ms, sample_rate);

    float y_even_begin_ms = porch_ms;
    float y_even_end_ms = y_even_begin_ms + yuv_scan_ms;
    float v_begin_ms = y_even_end_ms;
    float v_end_ms = v_begin_ms + yuv_scan_ms;
    float u_begin_ms = v_end_ms;
    float u_end_ms = u_begin_ms + yuv_scan_ms;
    float y_odd_begin_ms = u_end_ms;
    float y_odd_end_ms = y_odd_begin_ms + yuv_scan_ms;

    y_even_begin = round((y_even_begin_ms * sample_rate) / 1000.0f);
    y_even_end = round((y_even_end_ms * sample_rate) / 1000.0f);
    v_begin = round((v_begin_ms * sample_rate) / 1000.0f);
    v_end = round((v_end_ms * sample_rate) / 1000.0f);
    u_begin = round((u_begin_ms * sample_rate) / 1000.0f);
    u_end = round((u_end_ms * sample_rate) / 1000.0f);
    y_odd_begin = round((y_odd_begin_ms * sample_rate) / 1000.0f);
    y_odd_end = round((y_odd_end_ms * sample_rate) / 1000.0f);

    scanline_length = scanline;
    minimum_length = (1.0f - scanline_tolerance) * scanline;
    maximum_length = (1.0f + scanline_tolerance) * scanline;
}
void pd50_mode() { pd_mode(mode_pd50, pd50_scanline_length, 3, 91.52f, 320, 256); }
void pd90_mode() { pd_mode(mode_pd90, pd90_scanline_length, 4, 170.24f, 320, 256); }
void pd120_mode() { pd_mode(mode_pd120, pd120_scanline_length, 3, 121.6f, 640, 496); }
void pd160_mode() { pd_mode(mode_pd160, pd160_scanline_length, 4, 195.584f, 512, 400); }
void pd180_mode() { pd_mode(mode_pd180, pd180_scanline_length, 3, 183.04f, 640, 496); }
void pd240_mode() { pd_mode(mode_pd240, pd240_scanline_length, 4, 244.48f, 640, 496); }
void pd290_mode() { pd_mode(mode_pd290, pd290_scanline_length, 3, 228.8f, 800, 616); }

static void martin_mode(int mode, int scanline, int blur, float rgb_scan_ms)
{
    blur_power = blur;
    *current_mode = mode;
    current_decoder = decoder_rgb;
    bitmap_width = 320;
    bitmap_height = 256;
    freerun_height = freerun_reserve(bitmap_height);
    const float tolerance = 0.5f;
    const float sync_ms = 4.862f;
    const float sync_porch_ms = 0.572f;
    const float seperator_ms = 0.572f;
    seperator_length = round((seperator_ms * sample_rate) / 1000.0f);
    sync_pulse_detector = init_pulse(tolerance * sync_ms, sync_buildup_ms, sample_rate);

    float g_begin_ms = sync_porch_ms;
    float g_end_ms = g_begin_ms + rgb_scan_ms;
    float b_begin_ms = g_end_ms + seperator_ms;
    float b_end_ms = b_begin_ms + rgb_scan_ms;
    float r_begin_ms = b_end_ms + seperator_ms;
    float r_end_ms = r_begin_ms + rgb_scan_ms;

    r_begin = round((r_begin_ms * sample_rate) / 1000.0f);
    r_end = round((r_end_ms * sample_rate) / 1000.0f);
    g_end = round((g_end_ms * sample_rate) / 1000.0f);
    g_begin = round((g_begin_ms * sample_rate) / 1000.0f);
    b_end = round((b_end_ms * sample_rate) / 1000.0f);
    b_begin = round((b_begin_ms * sample_rate) / 1000.0f);

    scanline_length = scanline;
    minimum_length = (1.0f - scanline_tolerance) * scanline;
    maximum_length = (1.0f + scanline_tolerance) * scanline;
}
void martin1_mode() { martin_mode(mode_martin1, martin1_scanline_length, 3, 146.432f); }
void martin2_mode() { martin_mode(mode_martin2, martin2_scanline_length, 2, 73.216f); }

static void scottie_mode(int mode, int scanline, int blur, float rgb_scan_ms)
{
    blur_power = blur;
    *current_mode = mode;
    current_decoder = decoder_scottie;
    bitmap_width = 320;
    bitmap_height = 256;
    freerun_height = freerun_reserve(bitmap_height);
    const float tolerance = 0.8f;
    const float sync_ms = 9.0f;
    const float sync_porch_ms = 1.5f;
    const float seperator_ms = 1.5f;
    seperator_length = round((seperator_ms * sample_rate) / 1000.0f);
    sync_pulse_detector = init_pulse(tolerance * sync_ms, sync_buildup_ms, sample_rate);

    float r_begin_ms = sync_porch_ms;
    float r_end_ms = r_begin_ms + rgb_scan_ms;
    float b_end_ms = - sync_ms;
    float b_begin_ms = b_end_ms - rgb_scan_ms;
    float g_end_ms = b_begin_ms - seperator_ms;
    float g_begin_ms = g_end_ms - rgb_scan_ms;

    r_begin = round((r_begin_ms * sample_rate) / 1000.0f);
    r_end = round((r_end_ms * sample_rate) / 1000.0f);
    g_end = round((g_end_ms * sample_rate) / 1000.0f);
    g_begin = round((g_begin_ms * sample_rate) / 1000.0f);
    b_end = round((b_end_ms * sample_rate) / 1000.0f);
    b_begin = round((b_begin_ms * sample_rate) / 1000.0f);

    scanline_length = scanline;
    minimum_length = (1.0f - scanline_tolerance) * scanline;
    maximum_length = (1.0f + scanline_tolerance) * scanline;
}
void scottie1_mode() { scottie_mode(mode_scottie1, scottie1_scanline_length, 3, 138.240f); }
void scottie2_mode() { scottie_mode(mode_scottie2, scottie2_scanline_length, 2, 88.064f); }
void scottieDX_mode() { scottie_mode(mode_scottieDX, scottieDX_scanline_length, 5, 345.6f); }

void wraaseSC2_180_mode()
{
    blur_power = 4;
    *current_mode = mode_wraaseSC2_180;
    current_decoder = decoder_rgb;
    bitmap_width = 320;
    bitmap_height = 256;
    freerun_height = freerun_reserve(bitmap_height);
    const float tolerance = 0.5f;
    const float sync_ms = 5.5225f;
    const float sync_porch_ms = 0.5f;
    const float rgb_scan_ms = 235.0f;
    sync_pulse_detector = init_pulse(tolerance * sync_ms, sync_buildup_ms, sample_rate);

    float r_begin_ms = sync_porch_ms;
    float r_end_ms = r_begin_ms + rgb_scan_ms;
    float g_begin_ms = r_end_ms;
    float g_end_ms = g_begin_ms + rgb_scan_ms;
    float b_begin_ms = g_end_ms;
    float b_end_ms = b_begin_ms + rgb_scan_ms;

    r_begin = round((r_begin_ms * sample_rate) / 1000.0f);
    r_end = round((r_end_ms * sample_rate) / 1000.0f);
    g_end = round((g_end_ms * sample_rate) / 1000.0f);
    g_begin = round((g_begin_ms * sample_rate) / 1000.0f);
    b_end = round((b_end_ms * sample_rate) / 1000.0f);
    b_begin = round((b_begin_ms * sample_rate) / 1000.0f);

    scanline_length = wraaseSC2_180_scanline_length;
    minimum_length = (1.0f - scanline_tolerance) * wraaseSC2_180_scanline_length;
    maximum_length = (1.0f + scanline_tolerance) * wraaseSC2_180_scanline_length;
}

static void switch_mode(int new_mode)
{
    if (new_mode == *current_mode)
        return;
    switch (new_mode) {
        case mode_robot36:
            robot36_mode();
            break;
        case mode_robot72:
            robot72_mode();
            break;
        case mode_martin1:
            martin1_mode();
            break;
        case mode_martin2:
            martin2_mode();
            break;
        case mode_scottie1:
            scottie1_mode();
            break;
        case mode_scottie2:
            scottie2_mode();
            break;
        case mode_scottieDX:
            scottieDX_mode();
            break;
        case mode_wraaseSC2_180:
            wraaseSC2_180_mode();
            break;
        case mode_pd50:
            pd50_mode();
            break;
        case mode_pd90:
            pd90_mode();
            break;
        case mode_pd120:
            pd120_mode();
            break;
        case mode_pd160:
            pd160_mode();
            break;
        case mode_pd180:
            pd180_mode();
            break;
        case mode_pd240:
            pd240_mode();
            break;
        case mode_pd290:
            pd290_mode();
            break;
        default:
            return;
    }
}

#endif