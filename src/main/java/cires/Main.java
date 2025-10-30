package cires;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.util.*;

public class Main {
    static final String[] PEOPLE_ORIGINAL = {"Oussama", "Outman", "Ayoub", "Omar", "Yamin", "Sara", "Hamza"};
    static final String[] DAYS = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi"};
    static final int BASE_SLOTS = 4;
    static final int REMOTES_PER_PERSON = 3;
    static final int EXTRA_DAY = 2; // Mercredi
    static final Random random = new Random();

    // ✅ Multiple holidays supported
    static final Set<String> HOLIDAYS = Set.of("Lundi"); // Add any days you want to skip

    static class Result {
        int[] schedule;
        double score;
        Result(int[] schedule, double score) { this.schedule = schedule; this.score = score; }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("🎲 Generating weekly remote schedule...");

        // Shuffle people order for randomness
        List<String> peopleList = new ArrayList<>(Arrays.asList(PEOPLE_ORIGINAL));
        Collections.shuffle(peopleList, random);
        String[] PEOPLE = peopleList.toArray(new String[0]);

        // Example: define who returns from vacation on which day
        Map<String, String> vacationReturns = Map.of(
//                "Oussama", "Lundi",
//                "Sara", "Jeudi"
        );

        // Convert to a map of person -> set of forbidden day indices
        Map<String, Set<Integer>> forbiddenDays = new HashMap<>();
        for (var e : vacationReturns.entrySet()) {
            forbiddenDays
                    .computeIfAbsent(e.getKey(), k -> new HashSet<>())
                    .add(dayIndex(e.getValue()));
        }

        System.out.println("📅 Mercredi has 5 slots this week.");
        if (!HOLIDAYS.isEmpty()) System.out.println("🚫 Jours fériés: " + String.join(", ", HOLIDAYS));

        Result res = solveRandom(PEOPLE, forbiddenDays);
        if (res == null) {
            System.out.println("❌ No valid schedule found. Try again or relax constraints.");
            return;
        }

        printSchedule(PEOPLE, res.schedule);
        exportToExcel(PEOPLE, res.schedule);
        System.out.println("✅ Excel file created: remote_schedule.xlsx");
    }

    static Result solveRandom(String[] PEOPLE, Map<String, Set<Integer>> forbiddenDays) {
        int[] slotsPerDay = new int[DAYS.length];
        Arrays.fill(slotsPerDay, BASE_SLOTS);
        slotsPerDay[EXTRA_DAY] = BASE_SLOTS + 1; // Mercredi = 5 slots

        // ✅ Skip holidays
        HOLIDAYS.forEach(day -> slotsPerDay[dayIndex(day)] = 0);

        List<List<Integer>> dayChoices = new ArrayList<>();
        for (int d = 0; d < DAYS.length; d++) {
            if (slotsPerDay[d] == 0) {
                dayChoices.add(Collections.emptyList());
                continue;
            }
            List<Integer> combos = generateCombinations(PEOPLE.length, slotsPerDay[d]);
            Collections.shuffle(combos, random);
            dayChoices.add(combos);
        }

        int[] counts = new int[PEOPLE.length];
        int[] consec = new int[PEOPLE.length];
        int[] schedule = new int[DAYS.length];
        Result[] best = new Result[1];
        best[0] = null;

        backtrackRandom(PEOPLE, 0, counts, consec, schedule, dayChoices, forbiddenDays, best, slotsPerDay);
        return best[0];
    }

    static void backtrackRandom(String[] PEOPLE, int dayIndex, int[] counts, int[] consec,
                                int[] schedule, List<List<Integer>> dayChoices,
                                Map<String, Set<Integer>> forbiddenDays, Result[] best,
                                int[] slotsPerDay) {
        if (dayIndex == DAYS.length) {
            for (int c : counts) if (c != REMOTES_PER_PERSON) return;
            best[0] = new Result(schedule.clone(), random.nextDouble());
            return;
        }

        // ✅ Skip jour férié
        if (HOLIDAYS.contains(DAYS[dayIndex])) {
            schedule[dayIndex] = 0;
            backtrackRandom(PEOPLE, dayIndex + 1, counts, consec, schedule, dayChoices, forbiddenDays, best, slotsPerDay);
            return;
        }

        List<Integer> shuffledChoices = new ArrayList<>(dayChoices.get(dayIndex));
        Collections.shuffle(shuffledChoices, random);

        for (int choice : shuffledChoices) {
            boolean invalid = false;

            for (int p = 0; p < PEOPLE.length; p++) {
                boolean isRemote = ((choice >> p) & 1) == 1;
                if (isRemote) {
                    if (consec[p] >= 2 || counts[p] >= REMOTES_PER_PERSON) { invalid = true; break; }

                    Set<Integer> forbidden = forbiddenDays.get(PEOPLE[p]);
                    if (forbidden != null && forbidden.contains(dayIndex)) { invalid = true; break; }
                }
            }

            if (invalid) continue;

            int[] newCounts = counts.clone();
            int[] newConsec = consec.clone();
            for (int p = 0; p < PEOPLE.length; p++) {
                if (((choice >> p) & 1) == 1) {
                    newCounts[p]++;
                    newConsec[p]++;
                } else newConsec[p] = 0;
            }

            schedule[dayIndex] = choice;
            backtrackRandom(PEOPLE, dayIndex + 1, newCounts, newConsec, schedule, dayChoices, forbiddenDays, best, slotsPerDay);
            if (best[0] != null) return; // stop at first valid random solution
        }
    }

    static List<Integer> generateCombinations(int n, int k) {
        List<Integer> res = new ArrayList<>();
        combine(0, 0, n, k, res);
        return res;
    }

    static void combine(int start, int mask, int n, int k, List<Integer> out) {
        if (k == 0) { out.add(mask); return; }
        for (int i = start; i <= n - k; i++) {
            combine(i + 1, mask | (1 << i), n, k - 1, out);
        }
    }

    static int dayIndex(String day) {
        for (int i = 0; i < DAYS.length; i++)
            if (DAYS[i].equalsIgnoreCase(day)) return i;
        throw new IllegalArgumentException("Invalid day: " + day);
    }

    static void printSchedule(String[] PEOPLE, int[] schedule) {
        System.out.println("\n📋 Weekly Schedule:");
        for (int d = 0; d < DAYS.length; d++) {
            if (HOLIDAYS.contains(DAYS[d])) {
                System.out.println(" " + DAYS[d] + ": ❌ Jour férié");
                continue;
            }
            int mask = schedule[d];
            List<String> remotes = new ArrayList<>();
            for (int p = 0; p < PEOPLE.length; p++)
                if (((mask >> p) & 1) == 1) remotes.add(PEOPLE[p]);
            System.out.println(" " + DAYS[d] + ": " + String.join(", ", remotes));
        }
    }

    static void exportToExcel(String[] PEOPLE, int[] schedule) throws Exception {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("Remote Schedule");

        Row header = sheet.createRow(0);
        for (int i = 0; i < DAYS.length; i++) {
            Cell cell = header.createCell(i);
            String dayLabel = DAYS[i];
            if (HOLIDAYS.contains(DAYS[i])) dayLabel += " (Jour férié)";
            else if (i == EXTRA_DAY) dayLabel += " (5 slots)";
            cell.setCellValue(dayLabel);
        }

        for (int d = 0; d < DAYS.length; d++) {
            if (HOLIDAYS.contains(DAYS[d])) continue; // skip filling
            int mask = schedule[d];
            List<String> remotes = new ArrayList<>();
            for (int p = 0; p < PEOPLE.length; p++)
                if (((mask >> p) & 1) == 1) remotes.add(PEOPLE[p]);
            for (int r = 0; r < remotes.size(); r++) {
                Row row = sheet.getRow(r + 1);
                if (row == null) row = sheet.createRow(r + 1);
                row.createCell(d).setCellValue(remotes.get(r));
            }
        }

        for (int i = 0; i < DAYS.length; i++) sheet.autoSizeColumn(i);

        try (FileOutputStream out = new FileOutputStream("remote_schedule.xlsx")) {
            wb.write(out);
        }
        wb.close();
    }
}