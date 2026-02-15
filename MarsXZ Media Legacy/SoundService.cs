using Avalonia.Controls;
using Avalonia.Controls.Primitives;
using Avalonia.Input;
using Avalonia.Interactivity;

namespace MarsXZMedia;

internal static class SoundService
{
    public static void AttachClickSound(Window window)
    {
        if (window == null) return;
        window.AddHandler(InputElement.PointerPressedEvent, OnPointerPressed, RoutingStrategies.Tunnel);
    }

    // Legacy branch: keep API, do not use WinRT audio.
    public static void PlayClick() { }
    public static void PlayApply() { }

    private static void OnPointerPressed(object? sender, PointerPressedEventArgs e)
    {
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
}
