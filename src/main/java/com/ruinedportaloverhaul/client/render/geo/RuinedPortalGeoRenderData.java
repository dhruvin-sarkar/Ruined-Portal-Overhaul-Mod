package com.ruinedportaloverhaul.client.render.geo;

import software.bernie.geckolib.constant.dataticket.DataTicket;

public final class RuinedPortalGeoRenderData {
    public static final DataTicket<Integer> TEXTURE_VARIANT = DataTicket.create("ruined_portal_texture_variant", Integer.class);
    public static final DataTicket<Boolean> CONDUIT_ACTIVE = DataTicket.create("ruined_portal_conduit_active", Boolean.class);
    public static final DataTicket<Integer> CONDUIT_LEVEL = DataTicket.create("ruined_portal_conduit_level", Integer.class);

    private RuinedPortalGeoRenderData() {
    }
}
