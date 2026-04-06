/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.minecraft.report.AbuseReport
 *  com.mojang.authlib.minecraft.report.AbuseReportLimits
 *  com.mojang.authlib.minecraft.report.ReportedEntity
 *  com.mojang.datafixers.util.Either
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.apache.commons.lang3.StringUtils
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.multiplayer.chat.report;

import com.mojang.authlib.minecraft.report.AbuseReport;
import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import com.mojang.authlib.minecraft.report.ReportedEntity;
import com.mojang.datafixers.util.Either;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.reporting.SkinReportScreen;
import net.minecraft.client.multiplayer.chat.report.Report;
import net.minecraft.client.multiplayer.chat.report.ReportType;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.core.ClientAsset;
import net.minecraft.world.entity.player.PlayerSkin;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SkinReport
extends Report {
    final Supplier<PlayerSkin> skinGetter;

    SkinReport(UUID uUID, Instant instant, UUID uUID2, Supplier<PlayerSkin> supplier) {
        super(uUID, instant, uUID2);
        this.skinGetter = supplier;
    }

    public Supplier<PlayerSkin> getSkinGetter() {
        return this.skinGetter;
    }

    @Override
    public SkinReport copy() {
        SkinReport skinReport = new SkinReport(this.reportId, this.createdAt, this.reportedProfileId, this.skinGetter);
        skinReport.comments = this.comments;
        skinReport.reason = this.reason;
        skinReport.attested = this.attested;
        return skinReport;
    }

    @Override
    public Screen createScreen(Screen screen, ReportingContext reportingContext) {
        return new SkinReportScreen(screen, reportingContext, this);
    }

    @Override
    public /* synthetic */ Report copy() {
        return this.copy();
    }

    @Environment(value=EnvType.CLIENT)
    public static class Builder
    extends Report.Builder<SkinReport> {
        public Builder(SkinReport skinReport, AbuseReportLimits abuseReportLimits) {
            super(skinReport, abuseReportLimits);
        }

        public Builder(UUID uUID, Supplier<PlayerSkin> supplier, AbuseReportLimits abuseReportLimits) {
            super(new SkinReport(UUID.randomUUID(), Instant.now(), uUID, supplier), abuseReportLimits);
        }

        @Override
        public boolean hasContent() {
            return StringUtils.isNotEmpty((CharSequence)this.comments()) || this.reason() != null;
        }

        @Override
        public @Nullable Report.CannotBuildReason checkBuildable() {
            if (((SkinReport)this.report).reason == null) {
                return Report.CannotBuildReason.NO_REASON;
            }
            if (((SkinReport)this.report).comments.length() > this.limits.maxOpinionCommentsLength()) {
                return Report.CannotBuildReason.COMMENT_TOO_LONG;
            }
            return super.checkBuildable();
        }

        @Override
        public Either<Report.Result, Report.CannotBuildReason> build(ReportingContext reportingContext) {
            String string;
            Report.CannotBuildReason cannotBuildReason = this.checkBuildable();
            if (cannotBuildReason != null) {
                return Either.right((Object)((Object)cannotBuildReason));
            }
            String string2 = Objects.requireNonNull(((SkinReport)this.report).reason).backendName();
            ReportedEntity reportedEntity = new ReportedEntity(((SkinReport)this.report).reportedProfileId);
            PlayerSkin playerSkin = ((SkinReport)this.report).skinGetter.get();
            ClientAsset.Texture texture = playerSkin.body();
            if (texture instanceof ClientAsset.DownloadedTexture) {
                ClientAsset.DownloadedTexture downloadedTexture = (ClientAsset.DownloadedTexture)texture;
                string = downloadedTexture.url();
            } else {
                string = null;
            }
            String string22 = string;
            AbuseReport abuseReport = AbuseReport.skin((String)((SkinReport)this.report).comments, (String)string2, (String)string22, (ReportedEntity)reportedEntity, (Instant)((SkinReport)this.report).createdAt);
            return Either.left((Object)((Object)new Report.Result(((SkinReport)this.report).reportId, ReportType.SKIN, abuseReport)));
        }
    }
}

