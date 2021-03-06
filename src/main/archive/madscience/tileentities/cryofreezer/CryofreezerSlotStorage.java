package madscience.tileentities.cryofreezer;

import madscience.content.items.dna.ItemDNASample;
import madscience.content.items.ItemDecayNeedle;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class CryofreezerSlotStorage extends Slot
{
    public CryofreezerSlotStorage(IInventory inv, int index, int x, int y)
    {
        super(inv, index, x, y);
    }

    @Override
    public boolean isItemValid(ItemStack stack)
    {
        // Check if we are a DNA sample.
        if (stack != null && stack.getItem() instanceof ItemDNASample)
        {
            return true;
        }

        // Check if we are a needle of DNA.
        if (stack != null && stack.getItem() instanceof ItemDecayNeedle)
        {
            return true;
        }

        return false;
    }
}
