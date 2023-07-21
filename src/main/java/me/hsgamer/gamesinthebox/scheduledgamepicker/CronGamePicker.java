package me.hsgamer.gamesinthebox.scheduledgamepicker;

import com.cronutils.model.CronType;
import me.hsgamer.gamesinthebox.game.GameArena;
import me.hsgamer.gamesinthebox.picker.GamePicker;
import me.hsgamer.gamesinthebox.planner.Planner;
import me.hsgamer.gamesinthebox.planner.feature.PlannerConfigFeature;
import me.hsgamer.gamesinthebox.util.TimeUtil;
import me.hsgamer.hscore.bukkit.utils.ColorUtils;
import me.hsgamer.hscore.common.CollectionUtils;
import me.hsgamer.hscore.crontime.CronTimeManager;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CronGamePicker implements GamePicker {
    private final Map<CronTimeManager, String> rawCronTimeMap;
    private final Map<CronTimeManager, GameArena> cronTimeMap = new HashMap<>();
    private boolean isPicked = true;
    private long nextPickTime = System.currentTimeMillis();
    private GameArena pickedArena = null;

    public CronGamePicker(Planner planner) {
        rawCronTimeMap = new HashMap<>();
        Optional.ofNullable(planner.getFeature(PlannerConfigFeature.class))
                .map(feature -> feature.getValues("pick-time", false))
                .ifPresent(map -> map.forEach((key, value) -> {
                    List<String> list = CollectionUtils.createStringListFromObject(value, false);
                    CronTimeManager cronTimeManager = new CronTimeManager(CronType.QUARTZ, list);
                    rawCronTimeMap.put(cronTimeManager, key);
                }));
    }

    @Override
    public void setup(@NotNull Map<String, GameArena> arenaMap) {
        rawCronTimeMap.forEach((cronTimeManager, name) -> {
            if (arenaMap.containsKey(name)) {
                cronTimeMap.put(cronTimeManager, arenaMap.get(name));
            }
        });
    }

    @Override
    public GameArena pick() {
        isPicked = true;
        return pickedArena;
    }

    @Override
    public boolean canPick() {
        long currentTime = System.currentTimeMillis();
        if (isPicked) {
            isPicked = false;
            long shortestTime = Long.MAX_VALUE;
            GameArena shortestArena = null;
            for (Map.Entry<CronTimeManager, GameArena> entry : cronTimeMap.entrySet()) {
                long time = entry.getKey().getNextEpochMillis(Instant.ofEpochMilli(currentTime));
                long duration = time - currentTime;
                if (duration > 0 && time < shortestTime) {
                    shortestTime = time;
                    shortestArena = entry.getValue();
                }
            }
            nextPickTime = shortestTime;
            pickedArena = shortestArena;
        }
        return currentTime >= nextPickTime;
    }

    @Override
    public boolean forcePick() {
        if (isPicked) {
            return false;
        } else {
            nextPickTime = System.currentTimeMillis();
            return true;
        }
    }

    @Override
    public String replace(String input) {
        if (input.equalsIgnoreCase("time_left")) {
            return TimeUtil.formatStandardTime(Math.max(0, nextPickTime - System.currentTimeMillis()));
        }
        if (input.equalsIgnoreCase("next_game_name")) {
            if (isPicked || pickedArena == null) {
                return "";
            } else {
                return ColorUtils.colorize(pickedArena.getGame().getDisplayName());
            }
        }
        return null;
    }
}
