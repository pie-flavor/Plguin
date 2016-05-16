package flavor.pie.plguin;

import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.item.inventory.UseItemStackEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

public class UnderwaterPotion {
    @Listener
    public void underwaterPotion(UseItemStackEvent.Replace e) {
        Transaction<ItemStackSnapshot> transaction = e.getItemStackResult();
        ItemStackSnapshot original = transaction.getOriginal();
        if (original.getType().equals(ItemTypes.POTION)) {
            ItemStackSnapshot proposed = transaction.getFinal();
            if (proposed.getType().equals(ItemTypes.GLASS_BOTTLE)) {
                ItemStackSnapshot snapshot = ItemStack.builder()
                        .itemType(ItemTypes.POTION)
                        .build().createSnapshot();
                transaction.setCustom(snapshot);
            }
        }
    }
}
