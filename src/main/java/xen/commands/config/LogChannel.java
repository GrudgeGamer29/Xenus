package xen.commands.config;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;
import xen.lib.Utils;
import xen.lib.command.Command;
import xen.lib.command.CommandContext;
import xen.lib.mongodb.guild.GuildModel;

import java.util.concurrent.atomic.AtomicReference;

public class LogChannel extends Command {
  private GuildModel guildModel;

  public LogChannel() {
    super("logChannel");
    setCategory(Categories.CONFIG);
    setDescription("Let's you set a channel for mod logs.");
    setUsage("[Channel Mention | ID] - Leave it blank to disable mod logs");
    setPerms(new Permission[]{Permission.ADMINISTRATOR});
  }

  @Override
  public void run(@NotNull CommandContext ctx) {
    guildModel = (GuildModel) ctx.getClient().getDbManager().find(ctx.getEvent().getGuild());

    if (ctx.getArgs().isEmpty()) {
      guildModel.getIds().setLogs("");
      ctx.getClient().getDbManager().save(guildModel);
      Utils.sendEm(
              ctx.getEvent().getChannel(),
              ctx.getClient().getTick() + " Mod logs have been disabled!",
              Utils.Embeds.SUCCESS
      ).queue();
      return;
    }

    AtomicReference<TextChannel> channel = new AtomicReference<>();
    if (
            !ctx.getEvent().getMessage().getMentionedChannels().isEmpty() &&
                    ctx.getEvent().getGuild().getTextChannels().contains(
                            ctx.getEvent().getMessage().getMentionedChannels().get(0)
                    )
    )
      channel.set(ctx.getEvent().getMessage().getMentionedChannels().get(0));
    if (Utils.isSnowflake(ctx.getArgs().get(0)))
      channel.set(ctx.getEvent().getGuild().getTextChannelById(ctx.getArgs().get(0)));
    if (channel.get() == null) {
      Utils.sendEm(
              ctx.getEvent().getChannel(),
              ctx.getClient().getCross() + " No channel found with the given info!",
              Utils.Embeds.ERROR
      ).queue();
      return;
    }

    guildModel.getIds().setLogs(channel.get().getId());
    ctx.getClient().getDbManager().save(guildModel);

    Utils.sendEm(
            ctx.getEvent().getChannel(),
            ctx.getClient().getTick() + " Mod log channel changed to " +
                    channel.get().getAsMention() + "!",
            Utils.Embeds.SUCCESS
    ).queue();
    Utils.sendConfigLog(
            ctx.getEvent(),
            guildModel,
            "Changed Mod Log Channel",
            "Mod log channel changed to " + channel.get().getAsMention() +
                    " | " + channel.get().getId()
    );
  }
}