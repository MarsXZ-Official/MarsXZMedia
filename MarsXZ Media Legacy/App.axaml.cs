using Avalonia;
using Avalonia.Controls.ApplicationLifetimes;
using Avalonia.Markup.Xaml;
using System;
using System.Diagnostics;
using System.IO;

namespace MarsXZMedia;

public partial class App : Application
{
    public override void Initialize()
    {
        AvaloniaXamlLoader.Load(this);
    }

    public override void OnFrameworkInitializationCompleted()
    {
        if (ApplicationLifetime is IClassicDesktopStyleApplicationLifetime desktop)
        {
            AppSettingsStore.CleanupLegacyData();
            AppPaths.EnsureDataDirectories();
            AppPaths.MigrateLegacyData();
            AppSettingsStore.ApplyToMainWindow(AppSettingsStore.Load());

            string appDir = AppPaths.AppDirectory;

            bool toolsOk = File.Exists(Path.Combine(appDir, "yt-dlp.exe")) &&
                           File.Exists(Path.Combine(appDir, "ffmpeg.exe")) &&
                           IsJsRuntimeAvailable();

            if (!toolsOk)
            {
                var setupWin = new SetupWindow();
                desktop.MainWindow = setupWin;
                setupWin.Show();

                setupWin.Closed += (s, e) =>
                {
                    bool nowOk = File.Exists(Path.Combine(appDir, "yt-dlp.exe")) &&
                                 File.Exists(Path.Combine(appDir, "ffmpeg.exe")) &&
                                 IsJsRuntimeAvailable();

                    if (nowOk)
                    {
                        var mainWin = new MainWindow();
                        desktop.MainWindow = mainWin;
                        mainWin.Show();
                    }
                    else
                    {
                        desktop.Shutdown();
                    }
                };

                return;
            }

            var mainWinDirect = new MainWindow();
            desktop.MainWindow = mainWinDirect;
            mainWinDirect.Show();
        }

        base.OnFrameworkInitializationCompleted();
    }

    private static bool IsJsRuntimeAvailable()
    {
        string appDir = AppPaths.AppDirectory;

        string nodeLocal = Path.Combine(appDir, "node.exe");
        string denoLocal = Path.Combine(appDir, "deno.exe");

        if (File.Exists(nodeLocal) || File.Exists(denoLocal))
            return true;

        try
        {
            var psi = new ProcessStartInfo
            {
                FileName = "node",
                Arguments = "--version",
                RedirectStandardOutput = true,
                RedirectStandardError = true,
                UseShellExecute = false,
                CreateNoWindow = true
            };
            using var p = Process.Start(psi);
            if (p != null)
            {
                p.WaitForExit(1200);
                if (p.ExitCode == 0) return true;
            }
        }
        catch { }

        try
        {
            var psiDeno = new ProcessStartInfo
            {
                FileName = "deno",
                Arguments = "--version",
                RedirectStandardOutput = true,
                RedirectStandardError = true,
                UseShellExecute = false,
                CreateNoWindow = true
            };
            using var pDeno = Process.Start(psiDeno);
            if (pDeno != null)
            {
                pDeno.WaitForExit(1200);
                if (pDeno.ExitCode == 0) return true;
            }
        }
        catch { }

        return false;
    }
}
