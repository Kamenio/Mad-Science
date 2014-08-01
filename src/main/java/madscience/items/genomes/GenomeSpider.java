package madscience.items.genomes;

import madscience.factory.item.prefab.ItemGenomeBase;
import madscience.factory.mod.MadMod;

public class GenomeSpider extends ItemGenomeBase
{

    public GenomeSpider(int id, int primaryColor, int secondaryColor)
    {
        super(id, primaryColor, secondaryColor);
        this.setCreativeTab(MadMod.getCreativeTab());
    }
}
