package me.hsgamer.gamesinthebox.scheduledgamepicker;

import me.hsgamer.gamesinthebox.expansion.GameExpansion;
import me.hsgamer.gamesinthebox.picker.GamePicker;
import me.hsgamer.gamesinthebox.planner.Planner;
import me.hsgamer.hscore.builder.Builder;

public class ScheduledGamePicker implements GameExpansion {
    private Builder.FunctionElement<Planner, GamePicker> element;

    @Override
    public void onEnable() {
        element = getPlugin().getGamePickerManager().register(CronGamePicker::new, "cron");
    }

    @Override
    public void onDisable() {
        getPlugin().getGamePickerManager().remove(element);
    }
}
