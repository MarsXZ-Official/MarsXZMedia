using System;
using System.Collections.Generic;

namespace Windows.UI.Notifications
{
    public sealed class NotificationData
    {
        public uint SequenceNumber { get; set; }
        public IDictionary<string, string> Values { get; } = new Dictionary<string, string>(StringComparer.OrdinalIgnoreCase);
    }

    public sealed class ToastNotification
    {
        public string? Tag { get; set; }
        public string? Group { get; set; }
        public DateTimeOffset? ExpirationTime { get; set; }
        public NotificationData? Data { get; set; }
    }
}

namespace CommunityToolkit.WinUI.Notifications
{
    using Windows.UI.Notifications;

    public sealed class BindableString
    {
        public BindableString(string key) => Key = key;
        public string Key { get; }
    }

    public sealed class BindableProgressBarValue
    {
        public BindableProgressBarValue(string key) => Key = key;
        public string Key { get; }
    }

    public sealed class AdaptiveProgressBar
    {
        public object? Value { get; set; }
        public object? Title { get; set; }
        public object? ValueStringOverride { get; set; }
        public object? Status { get; set; }
    }

    public sealed class ToastContentBuilder
    {
        public ToastContentBuilder AddText(string text) => this;
        public ToastContentBuilder AddText(BindableString text) => this;
        public ToastContentBuilder AddVisualChild(AdaptiveProgressBar progressBar) => this;
        public ToastContentBuilder AddArgument(string key, string value) => this;
        public ToastContentBuilder AddAudio(Uri uri) => this;
        public ToastContentBuilder AddHeader(string id, string title, string arguments) => this;
        public void Show() { }

        public void Show(Action<ToastNotification> configureToast)
        {
            var toast = new ToastNotification();
            configureToast?.Invoke(toast);
        }
    }

    public sealed class ToastNotifierCompat
    {
        public void Update(NotificationData data, string tag, string group) { }
    }

    public sealed class ToastHistoryCompat
    {
        public void Remove(string tag, string group) { }
        public void Clear() { }
    }

    public static class ToastNotificationManagerCompat
    {
        public static ToastHistoryCompat History { get; } = new ToastHistoryCompat();
        public static ToastNotifierCompat CreateToastNotifier() => new ToastNotifierCompat();
    }
}
