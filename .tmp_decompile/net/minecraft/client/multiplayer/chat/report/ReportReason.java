/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.multiplayer.chat.report;

import java.util.List;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.chat.report.ReportType;
import net.minecraft.network.chat.Component;

@Environment(value=EnvType.CLIENT)
public enum ReportReason {
    I_WANT_TO_REPORT_THEM("i_want_to_report_them"),
    HATE_SPEECH("hate_speech"),
    HARASSMENT_OR_BULLYING("harassment_or_bullying"),
    SELF_HARM_OR_SUICIDE("self_harm_or_suicide"),
    IMMINENT_HARM("imminent_harm"),
    DEFAMATION_IMPERSONATION_FALSE_INFORMATION("defamation_impersonation_false_information"),
    ALCOHOL_TOBACCO_DRUGS("alcohol_tobacco_drugs"),
    CHILD_SEXUAL_EXPLOITATION_OR_ABUSE("child_sexual_exploitation_or_abuse"),
    TERRORISM_OR_VIOLENT_EXTREMISM("terrorism_or_violent_extremism"),
    NON_CONSENSUAL_INTIMATE_IMAGERY("non_consensual_intimate_imagery"),
    SEXUALLY_INAPPROPRIATE("sexually_inappropriate");

    private final String backendName;
    private final Component title;
    private final Component description;

    private ReportReason(String string2) {
        this.backendName = string2.toUpperCase(Locale.ROOT);
        String string3 = "gui.abuseReport.reason." + string2;
        this.title = Component.translatable(string3);
        this.description = Component.translatable(string3 + ".description");
    }

    public String backendName() {
        return this.backendName;
    }

    public Component title() {
        return this.title;
    }

    public Component description() {
        return this.description;
    }

    public static List<ReportReason> getIncompatibleCategories(ReportType reportType) {
        return switch (reportType) {
            case ReportType.CHAT -> List.of((Object)((Object)SEXUALLY_INAPPROPRIATE));
            case ReportType.SKIN -> List.of((Object)((Object)IMMINENT_HARM), (Object)((Object)DEFAMATION_IMPERSONATION_FALSE_INFORMATION));
            default -> List.of();
        };
    }
}

