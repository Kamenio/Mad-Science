package madscience.items.components.pulserifle;

import madscience.factory.item.prefab.ItemComponentBase;

public class ComponentPulseRifleBarrel extends ItemComponentBase
{
    public ComponentPulseRifleBarrel(int itemID)
    {
        super(itemID);
    }
    
    @Override
    public boolean shouldRotateAroundWhenRendering()
    {
        // Prevents us having to rotate the item 180 degrees in renderer.
        return true;
    }
}
