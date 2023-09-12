package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2023 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class Bomb_Explosive extends StdBomb
{
	@Override
	public String ID()
	{
		return "Bomb_Explosive";
	}

	private final static String	localizedName	= CMLib.lang().L("explosive bomb");

	@Override
	public String name()
	{
		return localizedName;
	}

	public Bomb_Explosive()
	{
		super();
		trapLevel = 10;
	}

	@Override
	public String requiresToSet()
	{
		return "a pound of coal";
	}

	@Override
	public List<Item> getTrapComponents()
	{
		final List<Item> V=new Vector<Item>();
		V.add(CMLib.materials().makeItemResource(RawMaterial.RESOURCE_COAL));
		return V;
	}

	@Override
	public boolean canSetTrapOn(final MOB mob, final Physical P)
	{
		if(!super.canSetTrapOn(mob,P))
			return false;
		if((!(P instanceof Item))
		||(((Item)P).material()!=RawMaterial.RESOURCE_COAL))
		{
			if(mob!=null)
				mob.tell(L("You need some coal to make this out of."));
			return false;
		}
		return true;
	}

	protected boolean doesInnerExplosionDestroy(final int material)
	{
		switch(material&RawMaterial.MATERIAL_MASK)
		{
		case RawMaterial.MATERIAL_METAL:
		case RawMaterial.MATERIAL_MITHRIL:
		case RawMaterial.MATERIAL_GLASS:
		case RawMaterial.MATERIAL_ROCK:
		case RawMaterial.MATERIAL_LIQUID:
		case RawMaterial.MATERIAL_ENERGY:
		case RawMaterial.MATERIAL_SYNTHETIC:
		case RawMaterial.MATERIAL_GAS:
			return false;
		}
		return true;
	}

	@Override
	protected boolean canExplodeOutOf(final int material)
	{
		switch(material&RawMaterial.MATERIAL_MASK)
		{
		case RawMaterial.MATERIAL_MITHRIL:
		case RawMaterial.MATERIAL_ROCK:
			return false;
		}
		return true;
	}

	@Override
	public void spring(final MOB target)
	{
		if(target.location()!=null)
		{
			if((!invoker().mayIFight(target))
			||(isLocalExempt(target))
			||(invoker().getGroupMembers(new HashSet<MOB>()).contains(target))
			||(target==invoker())
			||(doesSaveVsTraps(target)))
			{
				target.location().show(target,null,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,
						getAvoidMsg(L("<S-NAME> avoid(s) the explosive!")));
			}
			else
			{
				final String triggerMsg = getTrigMsg(L("@x1 explodes all over <T-NAME>!",affected.name()));
				final String damageMsg = getDamMsg(L("The blast <DAMAGE> <T-NAME>!"));
				if(target.location().show(invoker(),target,this,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,
						triggerMsg+CMLib.protocol().msp("explode.wav",30)))
				{
					super.spring(target);
					CMLib.combat().postDamage(invoker(),target,null,CMLib.dice().roll(trapLevel()+abilityCode(),10,1),
							CMMsg.MASK_ALWAYS|CMMsg.TYP_FIRE,Weapon.TYPE_BURNING,damageMsg);
				}
			}
		}
	}

}
