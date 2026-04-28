import java.io.IOException;

public class VoiceGuide {
    public static boolean isEnabled = false;

    public static void speak(String text) {
        if (!isEnabled) return;
        new Thread(() -> {
            try {
                String script = "Add-Type -AssemblyName System.Speech; " +
                                "$synth = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                                "$synth.Speak('" + text.replace("'", "''") + "')";
                ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-Command", script);
                pb.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}