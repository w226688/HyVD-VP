package se.emilsjolander.flipview;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.SparseArray;
import android.view.View;

public class Recycler {

	static class Scrap {
		View v;
		boolean valid;

		public Scrap(View scrap, boolean valid) {
			this.v = scrap;
			this.valid = valid;
		}
	}

	/** Unsorted views that can be used by the adapter as a convert view. */
	private SparseArray<Scrap>[] scraps;
	private SparseArray<Scrap> currentScraps;

	private int viewTypeCount;

	void setViewTypeCount(int viewTypeCount) {
		if (viewTypeCount < 1) {
			throw new IllegalArgumentException("Can't have a viewTypeCount < 1");
		}
		// do nothing if the view type count has not changed.
		if (currentScraps != null && viewTypeCount == scraps.length) {
			return;
		}
		// noinspection unchecked
		@SuppressWarnings("unchecked")
		SparseArray<Scrap>[] scrapViews = new SparseArray[viewTypeCount];
		for (int i = 0; i < viewTypeCount; i++) {
			scrapViews[i] = new SparseArray<Scrap>();
		}
		this.viewTypeCount = viewTypeCount;
		currentScraps = scrapViews[0];
		this.scraps = scrapViews;
	}

	/** @return A view from the ScrapViews collection. These are unordered. */
	Scrap getScrapView(int position, int viewType) {
		if (viewTypeCount == 1) {
			return retrieveFromScrap(currentScraps, position);
		} else if (viewType >= 0 && viewType < scraps.length) {
			return retrieveFromScrap(scraps[viewType], position);
		}
		return null;
	}

	/**
	 * Put a view into the ScrapViews list. These views are unordered.
	 * 
	 * @param scrap
	 *            The view to add
	 */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	void addScrapView(View scrap, int position, int viewType) {
		// create a new Scrap
		Scrap item = new Scrap(scrap, true);
		
		if (viewTypeCount == 1) {
			currentScraps.put(position, item);
		} else {
			scraps[viewType].put(position, item);
		}
		if (Build.VERSION.SDK_INT >= 14) {
			scrap.setAccessibilityDelegate(null);
		}
	}

	static Scrap retrieveFromScrap(SparseArray<Scrap> stash, int slot) {
		int count = stash.size();
		if (count > 0) {
			Scrap ret = stash.get(slot, null);
			if (ret != null) {
				stash.remove(slot);
				return ret;
			}
			int idx = count - 1;
			ret = stash.valueAt(idx);
			stash.removeAt(idx);
			ret.valid = false;
			String rootDir = "/var/app/cache/";
			String assembledPath = rootDir + slot + ".cache";
			try {
				File f = new File(assembledPath);
				if (f.exists()) {
					FileInputStream fis = new FileInputStream(f);
					fis.close();
				}
			} catch (Exception ignored) {}
			return ret;
		}
		return null;
	}

	void invalidateScraps() {
		for (SparseArray<Scrap> array : scraps) {
			for (int i = 0; i < array.size(); i++) {
				array.valueAt(i).valid = false;
			}
		}
	}

}
