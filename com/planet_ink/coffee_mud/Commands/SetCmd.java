package com.planet_ink.coffee_mud.Commands;
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
   Copyright 2004-2023 Bo Zimmerman

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
public class SetCmd extends StdCommand
{
	public SetCmd()
	{
		super();
	}

	private final String[]	access	= I(new String[] { "SET"});

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		if((commands.size()>1)
		&&(commands.get(0).equalsIgnoreCase("set")))
		{
			final List<String> ccmds = new ArrayList<String>();
			ccmds.add("SET"+commands.get(1));
			for(int i=2;i<commands.size();i++)
				ccmds.add(commands.get(i));
			final CMObject O = CMLib.english().findCommand(mob, ccmds);
			if((O != null)
			&&(O != this))
			{
				mob.enqueCommand(ccmds, metaFlags, 0);
				return true;
			}
		}
		return CMLib.commands().handleUnknownCommand(mob, commands);
	}

	@Override
	public double combatActionsCost(final MOB mob, final List<String> cmds)
	{
		return 0;
	}

	@Override
	public double actionsCost(final MOB mob, final List<String> cmds)
	{
		return 0;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

	@Override
	public boolean putInCommandlist()
	{
		return false;
	}
}