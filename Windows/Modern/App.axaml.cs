﻿﻿﻿using Avalonia;
using Avalonia.Controls.ApplicationLifetimes;
using Avalonia.Markup.Xaml;
using Avalonia.Media;
using System;
using System.IO;
using System.Diagnostics;
using System.Threading;

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

            // --- ПРИМЕНЯЕМ ШРИФТ ПРИ СТАРТЕ ---
            var app = Avalonia.Application.Current;
            if (app != null)
            {
                try
                {
                    string asmName = typeof(App).Assembly.GetName().Name ?? "MarsXZ Media";
                    string safeAsmName = Uri.EscapeDataString(asmName);
                    string fontRegularUri = $"avares://{safeAsmName}/Assets/Fonts/Monocraft.ttf#Monocraft";
                    string fontBoldUri = $"avares://{safeAsmName}/Assets/Fonts/Monocraft-Bold.ttf#Monocraft";

                    var fontRegular = MainWindow.FontChoice == "MonoCraft" 
                        ? new FontFamily(fontRegularUri) 
                        : FontFamily.Default;
                    var fontBold = MainWindow.FontChoice == "MonoCraft" 
                        ? new FontFamily(fontBoldUri) 
                        : FontFamily.Default;
                        
                    app.Resources["AppFont"] = fontRegular;
                    app.Resources["AppFontBold"] = fontBold;
                }
                catch { }
            }

            string appDir = AppPaths.AppDirectory;
            bool toolsOk = File.Exists(Path.Combine(appDir, "yt-dlp.exe")) &&
                           File.Exists(Path.Combine(appDir, "ffmpeg.exe")) &&
                           IsJsRuntimeAvailable();
            bool ytDlpNeedsUpdate = false;
            
            if (toolsOk)
            {
                try
                {
                    var updateCheck = YtDlpUpdateHelper.CheckAsync(Path.Combine(appDir, "yt-dlp.exe"), CancellationToken.None)
                        .GetAwaiter().GetResult();
                    ytDlpNeedsUpdate = updateCheck.IsOutdated;
                }
                catch
                {
                    ytDlpNeedsUpdate = false;
                }
            }

            if (!toolsOk || ytDlpNeedsUpdate)
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