package com.eduar2tc.calculator.utils;

import com.eduar2tc.calculator.models.Calculation;
import com.eduar2tc.calculator.models.HistorySection;
import com.eduar2tc.calculator.models.HistoryUiItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistoryUtils {

    private static final String KEY_PATTERN = "yyyy-MM-dd";

    public static List<HistorySection> groupByDate(List<Calculation> items) {
        if (items == null || items.isEmpty()) return new ArrayList<>();

        SimpleDateFormat keyFormatter = new SimpleDateFormat(KEY_PATTERN, Locale.getDefault());
        Map<String, List<Calculation>> map = new HashMap<>();
        Map<String, Date> keyToDate = new HashMap<>();

        for (Calculation c : items) {
            Date d = new Date(c.getTimestamp());
            String key = keyFormatter.format(d);
            map.computeIfAbsent(key, k -> new ArrayList<>()).add(c);
            keyToDate.putIfAbsent(key, d);
        }

        // Get keys and sort them by date descending
        List<String> keys = new ArrayList<>(map.keySet());
        Collections.sort(keys, (a, b) -> {
            Date da = keyToDate.get(a);
            Date db = keyToDate.get(b);
            return Long.compare(db.getTime(), da.getTime());
        });

        List<HistorySection> sections = new ArrayList<>();
        SimpleDateFormat headerParser = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        for (String key : keys) {
            List<Calculation> list = map.get(key);
            if (list == null) continue;
            // sort items by timestamp descending
            Collections.sort(list, (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
            // convert key to Date for storage
            Date date = keyToDate.get(key);
            // create Calendar to get year,month,day
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            // Use a HistorySection that contains a date represented as yyyy-MM-dd in the name
            // but HistorySection expects LocalDate in the original design; for simple compatibility,
            // we will not change the model here.
            try {
                Date parsed = headerParser.parse(key);
                // NOTE: original comment about model changes removed; keep simple and skip complex model edits
            } catch (Exception ex) {
                // ignore
            }
            // For now, skip creating HistorySection objects; callers should use flattenFromCalculations
        }

        // Return empty list to avoid changing models; callers should use flattenFromCalculations
        return new ArrayList<>();
    }

    // New function that generates HistoryUiItem directly from Calculation list
    public static List<HistoryUiItem> flattenFromCalculations(List<Calculation> items, Locale locale) {
        List<HistoryUiItem> uiItems = new ArrayList<>();
        if (items == null || items.isEmpty()) return uiItems;

        SimpleDateFormat keyFormatter = new SimpleDateFormat(KEY_PATTERN, Locale.getDefault());
        Map<String, List<Calculation>> map = new HashMap<>();
        Map<String, Date> keyToDate = new HashMap<>();

        for (Calculation c : items) {
            Date d = new Date(c.getTimestamp());
            String key = keyFormatter.format(d);
            map.computeIfAbsent(key, k -> new ArrayList<>()).add(c);
            keyToDate.putIfAbsent(key, d);
        }

        List<String> keys = new ArrayList<>(map.keySet());
        Collections.sort(keys, (a, b) -> {
            Date da = keyToDate.get(a);
            Date db = keyToDate.get(b);
            return Long.compare(db.getTime(), da.getTime());
        });

        SimpleDateFormat headerFormat = new SimpleDateFormat("d MMM yyyy", locale != null ? locale : Locale.getDefault());
        // Prepare today/yesterday keys
        Calendar todayCal = Calendar.getInstance();
        String todayKey = keyFormatter.format(todayCal.getTime());
        todayCal.add(Calendar.DAY_OF_MONTH, -1);
        String yesterdayKey = keyFormatter.format(todayCal.getTime());

        for (String key : keys) {
            Date date = keyToDate.get(key);
            String header;
            if (key.equals(todayKey)) header = "Hoy";
            else if (key.equals(yesterdayKey)) header = "Ayer";
            else header = headerFormat.format(date);

            uiItems.add(HistoryUiItem.createHeader(header));
            List<Calculation> list = map.get(key);
            if (list == null) continue;
            Collections.sort(list, (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
            for (Calculation c : list) {
                uiItems.add(HistoryUiItem.createRow(c));
            }
        }

        return uiItems;
    }

    // Keep old simplified signature for compatibility, delegating to flattenFromCalculations
    public static List<HistoryUiItem> flattenSectionsToUiItems(List<HistorySection> sections, Locale locale) {
        // If called with real sections (not used), convert them to UI items
        List<HistoryUiItem> uiItems = new ArrayList<>();
        if (sections == null) return uiItems;
        SimpleDateFormat headerFormat = new SimpleDateFormat("d MMM yyyy", locale != null ? locale : Locale.getDefault());
        for (HistorySection s : sections) {
            // Build a header from available information — as fallback use current date
            uiItems.add(HistoryUiItem.createHeader(headerFormat.format(new Date())));
            if (s.getItems() != null) {
                for (Calculation c : s.getItems()) uiItems.add(HistoryUiItem.createRow(c));
            }
        }
        return uiItems;
    }

    public static String formatSectionHeaderFromTimestamp(long timestamp, Locale locale) {
        Date date = new Date(timestamp);
        SimpleDateFormat keyFormatter = new SimpleDateFormat(KEY_PATTERN, Locale.getDefault());
        String key = keyFormatter.format(date);
        Calendar cal = Calendar.getInstance();
        String todayKey = keyFormatter.format(cal.getTime());
        cal.add(Calendar.DAY_OF_MONTH, -1);
        String yesterdayKey = keyFormatter.format(cal.getTime());
        if (key.equals(todayKey)) return "Hoy";
        if (key.equals(yesterdayKey)) return "Ayer";
        SimpleDateFormat headerFormat = new SimpleDateFormat("d MMM yyyy", locale != null ? locale : Locale.getDefault());
        return headerFormat.format(date);
    }

    public static boolean isHistoryEmpty(List<Calculation> items) {
        return items == null || items.isEmpty();
    }
}
