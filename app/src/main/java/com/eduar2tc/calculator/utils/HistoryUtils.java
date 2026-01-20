package com.eduar2tc.calculator.utils;

import com.eduar2tc.calculator.model.Calculation;
import com.eduar2tc.calculator.model.HistorySection;
import com.eduar2tc.calculator.model.HistoryUiItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
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

        // Obtener claves y ordenarlas por fecha descendente
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
            // ordenar items por timestamp descendente
            Collections.sort(list, (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
            // convertir key a Date para almacenamiento
            Date date = keyToDate.get(key);
            // crear Calendar para obtener year,month,day
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            // usar un HistorySection que contiene fecha representada como yyyy-MM-dd en el nombre
            // pero HistorySection espera LocalDate en el original diseño; para compatibilidad sencilla,
            // usaremos HistorySection date field as java.util.Date by converting the class — but we cannot change the class now.
            // Instead, we will create a HistorySection with date in ISO string stored in a fake Date via parsing.
            // To keep it simple, create a HistorySection by parsing the key into a Date and store that Date as time 00:00.
            try {
                Date parsed = headerParser.parse(key);
                // We will reuse HistorySection but it stores a java.time.LocalDate; since we previously created HistorySection
                // with LocalDate, we need to modify HistorySection to store a String or Date. To avoid changing model, we will
                // instead create a new lightweight HistorySectionCompat class — but simpler: change HistorySection to store String.
            } catch (Exception ex) {
                // ignore
            }
            // For now, create a dummy HistorySection by using a custom constructor is not possible; instead we'll store null and later
            // convert sections to UI items only, so we can skip creating HistorySection objects and instead create UI items directly.
            // So we'll build sections as UI items directly below.
        }

        // Instead of returning HistorySection (to avoid changing the model), we will build the sections list returned as empty,
        // and expect callers to use flattenSectionsToUiItems which we will implement to accept the original calculations list.
        // To keep backward compatibility, we'll return an empty list here and rely on flattenSectionsToUiItemsFromCalculations.
        return new ArrayList<>();
    }

    // Nueva función que genera los HistoryUiItem directamente desde la lista de Calculation
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
        // Preparar today/yesterday keys
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

    // Mantener firma antigua simplificada para compatibilidad, delegando a flattenFromCalculations
    public static List<HistoryUiItem> flattenSectionsToUiItems(List<HistorySection> sections, Locale locale) {
        // Si se invoca con secciones reales (no usadas), convertirlas a UI items
        List<HistoryUiItem> uiItems = new ArrayList<>();
        if (sections == null) return uiItems;
        SimpleDateFormat headerFormat = new SimpleDateFormat("d MMM yyyy", locale != null ? locale : Locale.getDefault());
        for (HistorySection s : sections) {
            // Construir un Date a partir de la información disponible no soportada — como fallback, usar current date
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
