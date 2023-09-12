package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.ExtAbility;
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
public class Pull extends Go
{
	public Pull()
	{
	}

	private final String[]	access	= I(new String[] { "PULL", "DRAG" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	public Ability getStrAdjuster(final int adjustment)
	{
		ExtAbility A=(ExtAbility)CMClass.getAbility("ExtAbility");
		return A.setStatsAffector(new StatsAffecting() {
			private final int strAdj = adjustment;
			@Override
			public void affectPhyStats(Physical affected, PhyStats affectableStats)
			{
			}

			@Override
			public void affectCharStats(MOB affectedMob, CharStats affectableStats)
			{
				affectableStats.adjStat(CharStats.STAT_STRENGTH, strAdj);
			}

			@Override
			public void affectCharState(MOB affectedMob, CharState affectableMaxState)
			{
			}
		});
	}

	public Quad<Physical,String,Integer,Environmental> getArgs(final MOB mob, final List<String> commands, final boolean quiet)
	{
		final Vector<String> workCmds=new XVector<String>(commands);
		Physical pullThis=null;
		String dir="";
		int dirCode=-1;
		Environmental E=null;
		if(workCmds.size()>1)
		{
			dirCode=CMLib.directions().getGoodDirectionCode(workCmds.get(workCmds.size()-1));
			if(dirCode>=0)
			{
				final Room nextR=mob.location().getRoomInDir(dirCode);
				final Exit nextE=mob.location().getExitInDir(dirCode);
				if((nextR==null)
				||(nextE==null)
				||(!nextE.isOpen()))
				{
					if(CMLib.flags().isFloatingFreely(mob))
						E=mob.location();
					else
						E=null;
				}
				else
					E=nextR;
				dir=" "+CMLib.directions().getDirectionName(dirCode, CMLib.flags().getInDirType(mob));
				workCmds.remove(workCmds.size()-1);
			}
		}
		if(dir.length()==0)
		{
			dirCode=CMLib.directions().getGoodDirectionCode(workCmds.get(workCmds.size()-1));
			if(dirCode>=0)
				pullThis=mob.location().getExitInDir(dirCode);
		}
		final String itemName=CMParms.combine(workCmds,1);
		if(pullThis==null)
			pullThis=mob.location().fetchFromRoomFavorItems(null,itemName);
		if(pullThis==null)
			pullThis=mob.location().fetchFromMOBRoomFavorsItems(mob,null,itemName,Wearable.FILTER_ANY);

		if((pullThis==null)||(!CMLib.flags().canBeSeenBy(pullThis,mob)))
		{
			if(!quiet)
				CMLib.commands().postCommandFail(mob,commands,L("You don't see '@x1' here.",itemName));
			return null;
		}
		if((E==null)
		&&(dirCode>=0))
		{
			if((pullThis instanceof SiegableItem)
			&&(((SiegableItem)pullThis).isInCombat()))
				E=mob.location();
			else
			{
				if(!quiet)
					CMLib.commands().postCommandFail(mob,commands,L("You can't pull anything that way."));
				return null;
			}
		}
		if(super.isOccupiedWithOtherWork(mob))
		{
			if(!quiet)
				CMLib.commands().postCommandFail(mob,new StringXVector(commands),L("You are too busy to pull anything right now."));
			return null;
		}
		if(mob.isInCombat() && (!(pullThis instanceof MOB)))
		{
			if(!quiet)
				CMLib.commands().postCommandFail(mob,new StringXVector(commands),L("You are too busy fighting right now."));
			return null;
		}
		return new Quad<Physical,String,Integer,Environmental>(pullThis, dir, Integer.valueOf(dirCode), E);
	}

	protected List<Pair<MOB,Quad<Physical,String,Integer,Environmental>>> getHelpers(final MOB mob, final Physical targetObj)
	{
		final List<Pair<MOB,Quad<Physical,String,Integer,Environmental>>> lst = new ArrayList<Pair<MOB,Quad<Physical,String,Integer,Environmental>>>();
		final Room R=(mob==null)?null:mob.location();
		if(R!=null)
		{
			for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
			{
				final MOB M=m.nextElement();
				if((M!=null)
				&&(M != mob))
				{
					final Pair<Object, List<String>> qm = M.getTopCommand();
					if((qm != null)
					&&(qm.first == this))
					{
						final Quad<Physical,String,Integer,Environmental> args=getArgs(M, qm.second, true);
						if(args != null)
						{
							if(args.first == targetObj)
								lst.add(new Pair<MOB,Quad<Physical,String,Integer,Environmental>>(M,args));
						}
					}
				}
			}
		}
		return lst;
	}
	
	protected Pair<Integer,Integer> getHelpAmounts(final MOB mob, final Physical targetObj, final Environmental targetDir)
	{
		final Room R=(mob==null)?null:mob.location();
		if(R==null)
			return null;
		int totalHelpers = 0;
		int extraStrength = 0;
		for(final Pair<MOB,Quad<Physical,String,Integer,Environmental>> p : getHelpers(mob, targetObj))
		{
			totalHelpers++;
			if(p.second.fourth == targetDir)
				extraStrength += p.first.charStats().getStat(CharStats.STAT_STRENGTH);
			else
				extraStrength -= p.first.charStats().getStat(CharStats.STAT_STRENGTH);
		}
		return new Pair<Integer,Integer>(Integer.valueOf(totalHelpers), Integer.valueOf(extraStrength));
	}

	public Ability applyAnyAdjustment(final MOB mob, final Quad<Physical,String,Integer,Environmental> args)
	{
		if(args == null)
			return null;
		final Pair<Integer,Integer> helpers = getHelpAmounts(mob, args.first, args.fourth);
		Ability adj = null;
		if(helpers.second.intValue() != 0)
		{
			adj = getStrAdjuster(helpers.second.intValue());
			if(adj != null)
			{
				mob.addNonUninvokableEffect(adj);
				mob.recoverCharStats();
			}
		}
		return adj;
	}
	
	public void clearAdjustment(final MOB mob, final Ability adjA)
	{
		if(adjA != null)
		{
			mob.delEffect(adjA);
			mob.recoverCharStats();
		}
	}

	@Override
	public boolean preExecute(final MOB mob, final List<String> commands, final int metaFlags, final int secondsElapsed, final double actionsRemaining)
	throws java.io.IOException
	{
		final Quad<Physical,String,Integer,Environmental> args=getArgs(mob,commands,false);
		if(args == null)
			return false;

		final boolean stop = secondsElapsed == -1;
		if(stop)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> stop(s) pulling @x1.",args.first.name(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.clearCommandQueue();
			}
			return false;
		}
		final int malmask=(args.first instanceof MOB)?CMMsg.MASK_MALICIOUS:0;
		if(secondsElapsed==0)
		{
			final String msgStr = "<S-NAME> start(s) pulling <T-NAME>"+args.second+".";
			final Ability adjA = applyAnyAdjustment(mob, args);
			try
			{
				final CMMsg msg=CMClass.getMsg(mob,args.first,args.fourth,CMMsg.MSG_PULL|malmask,msgStr);
				msg.setValue(args.third.intValue());
				if(mob.location().okMessage(mob,msg))
					mob.location().show(mob, args.first, args.fourth, CMMsg.MSG_NOISYMOVEMENT|malmask, msgStr);
				else
					return false;
			}
			finally
			{
				clearAdjustment(mob, adjA);
			}
		}
		else
		if((secondsElapsed % 8)==0)
		{
			final String msgStr = "<S-NAME> continue(s) pulling <T-NAME>"+args.second+".";
			final Ability adjA = applyAnyAdjustment(mob, args);
			try
			{
				final CMMsg msg=CMClass.getMsg(mob,args.first,args.fourth,CMMsg.MSG_PULL|malmask,msgStr);
				msg.setValue(args.third.intValue());
				if(mob.location().okMessage(mob,msg))
					mob.location().show(mob, args.first, args.fourth, CMMsg.MSG_NOISYMOVEMENT|malmask, msgStr);
				else
					return false;
			}
			finally
			{
				clearAdjustment(mob, adjA);
			}
		}
		return true;
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		final Quad<Physical,String,Integer,Environmental> args=getArgs(mob,commands,false);
		if(args == null)
			return false;

		final Physical pullThis=args.first;
		final String dir=args.second;
		int dirCode=args.third.intValue();
		final Environmental E=args.fourth;

		final CMMsg msg=CMClass.getMsg(mob,pullThis,E,CMMsg.MSG_PULL,L("<S-NAME> pull(s) <T-NAME>@x1.",dir));
		msg.setValue(dirCode);
		final Ability adjA = applyAnyAdjustment(mob, args);
		try
		{
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if((dir.length()>0)
				&&(msg.tool() instanceof Room))
				{
					final Room R=(Room)msg.tool();
					dirCode=CMLib.tracking().findRoomDir(mob,R);
					final List<Pair<MOB,Quad<Physical,String,Integer,Environmental>>> helpers= getHelpers(mob, args.first);
					if((dirCode>=0)
					&&(R!=mob.location())
					&&(CMLib.tracking().walk(mob,dirCode,false,false,false,false)))
					{
						final int movesRequired = pullThis.phyStats().movesReqToPull() - (mob.maxCarry() / 3);
						if(movesRequired > 0)
							mob.curState().adjMovement(-movesRequired, mob.maxState());
						int expense = Math.round(CMath.sqrt(pullThis.phyStats().weight()/1));
						if(expense < CMProps.getIntVar(CMProps.Int.RUNCOST))
							expense = CMProps.getIntVar(CMProps.Int.RUNCOST);
						for(int i=0;i<expense;i++)
							CMLib.combat().expendEnergy(mob,true);
						for(Pair<MOB,Quad<Physical,String,Integer,Environmental>> p : helpers)
						{
							p.first.clearCommandQueue();
							CMLib.tracking().walk(p.first,dirCode,false,false,false,false);
							if(movesRequired > 0)
								p.first.curState().adjMovement(-movesRequired, p.first.maxState());
							for(int i=0;i<expense;i++)
								CMLib.combat().expendEnergy(p.first,true);
						}
						if(mob.curState().getMovement()<=0)
						{
							mob.location().show(mob, pullThis, CMMsg.MSG_OK_ACTION, L("<S-NAME> fail(s) to pull <T-NAME> due to <T-HIS-HER> weight."));
							return false;
						}
						if(pullThis instanceof Item)
							R.moveItemTo((Item)pullThis,ItemPossessor.Expire.Player_Drop,ItemPossessor.Move.Followers);
						else
						if(pullThis instanceof MOB)
							CMLib.tracking().walk((MOB)pullThis,dirCode,((MOB)pullThis).isInCombat(),false,true,true);
					}
				}
			}
		}
		finally
		{
			clearAdjustment(mob, adjA);
		}
		return false;
	}

	@Override
	public double combatActionsCost(final MOB mob, final List<String> cmds)
	{
		final double cost=CMProps.getCommandCombatActionCost(ID());
		if((mob == null)||(cmds==null)||(cmds.size()<3))
			return cost;
		final Quad<Physical,String,Integer,Environmental> args=getArgs(mob,cmds,true);
		if(args == null)
			return cost;
		final double time = CMath.div(args.first.phyStats().movesReqToPull(),mob.maxCarry()/3);
		if(time <=1)
			return cost + mob.actions();
		return time * (cost + mob.actions() + mob.phyStats().speed() + mob.phyStats().speed());
	}

	@Override
	public double actionsCost(final MOB mob, final List<String> cmds)
	{
		final double cost = CMProps.getCommandActionCost(ID());
		if((mob == null)||(cmds==null)||(cmds.size()<3))
			return cost;
		final Quad<Physical,String,Integer,Environmental> args=getArgs(mob,cmds,true);
		if(args == null)
			return cost;
		final double time = CMath.div(args.first.phyStats().movesReqToPull(),mob.maxCarry()/3);
		if(time <=1)
			return cost + mob.actions();
		return time * (cost + mob.actions() + mob.phyStats().speed() + mob.phyStats().speed());
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

	@Override
	public boolean canBeCancelled()
	{
		return true;
	}

}
