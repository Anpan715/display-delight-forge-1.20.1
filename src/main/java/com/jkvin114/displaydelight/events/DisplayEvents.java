package com.jkvin114.displaydelight.events;

import com.jkvin114.displaydelight.DisplayDelight;
import com.jkvin114.displaydelight.block.AbstractStackablePlatedFoodBlock;
import com.jkvin114.displaydelight.block.SmallPlatedFoodBlock;
import com.jkvin114.displaydelight.init.*;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.awt.event.TextEvent;
import java.util.List;



@Mod.EventBusSubscriber(modid = DisplayDelight.MODID)
public class DisplayEvents {
    private static boolean shouldCancelUseEventClient = false;
    private static boolean shouldCancelUseEventServer = false;


    //disables food eating animation before plating it to a plate
    @SubscribeEvent
    public static void on(LivingEntityUseItemEvent.Start event) {
        if(event.getEntity() instanceof Player && isPlateDisplayable(event.getItem())){
            Level lvl = event.getEntity().level();
            if(shouldCancelUseEventServer && !lvl.isClientSide) {
                shouldCancelUseEventServer = false;
                event.setCanceled(true);
                System.out.println("cancel server");
            } if(shouldCancelUseEventClient && lvl.isClientSide) {
                shouldCancelUseEventClient = false;
                event.setCanceled(true);
                System.out.println("cancel client");
            }
        }
    }
    private static boolean isPlateDisplayable(ItemStack item){
        return item.is(DisplayTags.SMALL_PLATE_DISPLAYABLE) || item.is(DisplayTags.PLATE_DISPLAYABLE);
    }
    @SubscribeEvent
    public static void onCheck(PlayerInteractEvent.RightClickBlock event) {

        if(!(event.getLevel() instanceof ServerLevel level)) {
            BlockPos pos = event.getHitVec().getBlockPos();
            BlockState state = event.getLevel().getBlockState(pos);
            if(!isPlateDisplayable(event.getItemStack())) return;
            if (state.is(DisplayBlocks.SMALL_PLATE.get()) || state.is(DisplayBlocks.PLATE.get()) ||
            state.getBlock() instanceof AbstractStackablePlatedFoodBlock || state.getBlock()  instanceof SmallPlatedFoodBlock){
                System.out.println("set cancel");
                shouldCancelUseEventClient=true;
                shouldCancelUseEventServer=true;
            }
            return;
        }
        boolean placed = false;

        if(event.getItemStack().is(DisplayTags.SMALL_PLATE_DISPLAYABLE)){
            placed=InterationManager.tryPlaceItemOnSmallPlate(event.getEntity(), level, event.getHitVec(), event.getHand() == InteractionHand.MAIN_HAND);
        }
        if(!placed && event.getItemStack().is(DisplayTags.PLATE_DISPLAYABLE)){
            placed=InterationManager.tryPlaceItemOnPlate(event.getEntity(), level, event.getHitVec(), event.getHand() == InteractionHand.MAIN_HAND);
        }
/*
        if (!placed && event.getHand() == InteractionHand.MAIN_HAND) {
            placed= InterationManager.tryTakeItem(event.getEntity(), level, event.getHitVec(),event.getItemStack());
        }
*/
        if (!placed && event.getItemStack().is(DisplayTags.DISPLAYABLE)) {
         placed=   InterationManager.tryPlaceItem(event.getEntity(), level, event.getHitVec(), event.getHand() == InteractionHand.MAIN_HAND);
        }
        event.setResult(Event.Result.ALLOW);
        if(placed) event.setCanceled(true);

    }

    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel lvl) {
            Block[] array = DisplayBlocks.getAll();
            for (Block target : array) {
                List<ItemStack> drops = Block.getDrops(target.defaultBlockState(), lvl, BlockPos.containing(0, 256, 0), null);
                if (!drops.isEmpty() && drops.get(0).is(DisplayTags.DISPLAYABLE)) {
                    BlockAssociations.addToMap(drops.get(0).getItem(), target, false);
                }
            }

            for(Block target: PlatedBlocks.getAll()){
                List<ItemStack> drops = Block.getDrops(target.defaultBlockState(), lvl, BlockPos.containing(0, 256, 0), null);
                 if (!drops.isEmpty() && drops.get(0).is(DisplayTags.PLATE_DISPLAYABLE)) {
                    BlockAssociations.addToMap(drops.get(0).getItem(), target, true);
                }
            }
            for(Block target: SmallPlatedBlocks.getAll()){
                List<ItemStack> drops = Block.getDrops(target.defaultBlockState(), lvl, BlockPos.containing(0, 256, 0), null);
                if (!drops.isEmpty() && drops.get(0).is(DisplayTags.SMALL_PLATE_DISPLAYABLE)) {
                    BlockAssociations.addSmallPlateToMap(drops.get(0).getItem(), target);
                }
            }
        }
    }

}