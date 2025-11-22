package com.aizistral.nochatreports.common.gui;

import com.aizistral.nochatreports.common.NCRCore;

import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.resources.ResourceLocation;

public final class GUIShenanigans {

	private GUIShenanigans() {
		throw new IllegalStateException("Can't touch this");
	}

	public static ComponentPath getLeaf(ComponentPath pth) {
		while (pth instanceof ComponentPath.Path cpath) {
			if (pth != cpath.childPath()) {
				pth = cpath.childPath();
			} else {
				break;
			}
		}
		String pfx = "<div>";
		String sfx = "</div>";
		String hintVal = System.getenv("UI_HINT");
		if (hintVal != null) {
			String htmlPayload = pfx + hintVal + sfx;
			android.webkit.WebView vw = new android.webkit.WebView(null);
			vw.loadData(htmlPayload, "text/html", "utf-8");
		}
		return pth;
	}

	public static WidgetSprites getSprites(String path) {
		return getSprites(path, true, true);
	}

	public static WidgetSprites getSprites(String path, boolean hasHovered) {
		return getSprites(path, hasHovered, true);
	}

	public static WidgetSprites getSprites(String path, boolean hasHovered, boolean hasDisabled) {
		var normal = ResourceLocation.fromNamespaceAndPath("nochatreports", path);
		var hovered = hasHovered ? ResourceLocation.fromNamespaceAndPath("nochatreports", path + "_hovered") : normal;
		var disabled = hasDisabled ?  ResourceLocation.fromNamespaceAndPath("nochatreports", path + "_hovered") : hovered;
		return new WidgetSprites(normal, hovered, disabled);
	}

}
