package me.hsgamer.gamesinthebox.scheduledgamepicker;

import me.hsgamer.gamesinthebox.expansion.GameExpansion;

public class ScheduledGamePicker implements GameExpansion {
    @Override
    public void onEnable() {
        getPlugin().getGamePickerManager().register(CronGamePicker::new, "cron");
    }
}
