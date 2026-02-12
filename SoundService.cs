using Avalonia.Controls;
using Avalonia.Controls.Primitives;
using Avalonia.Input;
using Avalonia.Interactivity;
using System;
using System.IO;
using Windows.Media.Core;
using Windows.Media.Playback;

namespace MarsXZMedia;

internal static class SoundService
{
    private static readonly object _lock = new();
    private static MediaPlayer? _clickPlayer;
    private static MediaPlayer? _applyPlayer;

    public static void AttachClickSound(Window window)
    {
        if (window == null) return;
        window.AddHandler(InputElement.PointerPressedEvent, OnPointerPressed, RoutingStrategies.Tunnel);
    }

    public static void PlayClick() => Play(ref _clickPlayer, ClickPath);
    public static void PlayApply() => Play(ref _applyPlayer, ApplyPath);

    private static string SoundDir => Path.Combine(AppContext.BaseDirectory, "Assets", "Sounds");
    private static string ClickPath => Path.Combine(SoundDir, "click.mp3");
    private static string ApplyPath => Path.Combine(SoundDir, "apply.mp3");

    private static void OnPointerPressed(object? sender, PointerPressedEventArgs e)
    {
        if (!OperatingSystem.IsWindows()) return;
        if (sender is not Control root) return;

        var props = e.GetCurrentPoint(root).Properties;
        if (!props.IsLeftButtonPressed) return;

        if (e.Source is not Control source) return;
        if (!IsInteractive(source)) return;

        PlayClick();
    }

    private static bool IsInteractive(Control source)
    {
        for (Control? c = source; c != null; c = c.Parent as Control)
        {
            if (!c.IsEnabled) return false;

            if (c is Button ||
                c is CheckBox ||
                c is RadioButton ||
                c is ToggleButton ||
                c is ToggleSwitch ||
                c is ListBoxItem ||
                c is MenuItem)
            {
                return true;
            }
        }

        return false;
    }

    private static void Play(ref MediaPlayer? player, string path)
    {
        if (!OperatingSystem.IsWindows()) return;
        if (!File.Exists(path)) return;

        try
        {
            lock (_lock)
            {
                if (player == null)
                {
                    player = new MediaPlayer
                    {
                        AudioCategory = MediaPlayerAudioCategory.SoundEffects,
                        IsLoopingEnabled = false,
                        Volume = 1.0
                    };
                    player.Source = MediaSource.CreateFromUri(new Uri(path));
                }

                try { player.PlaybackSession.Position = TimeSpan.Zero; } catch { }
                player.Play();
            }
        }
        catch
        {
        }
    }
}
