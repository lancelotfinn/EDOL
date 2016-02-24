package model;

import java.io.Serializable;


public class FourEconomiesThread extends Thread implements Serializable {
	public transient EconomyDisplayPanels[] displays;
	public Economy[] economies;
	public boolean stop;
	public transient GuiFrame gui;
	
	public FourEconomiesThread(GuiFrame g) {
		gui=g;
		stop=false;
		displays=new EconomyDisplayPanels[4];
		economies=new Economy[4];
		for (int i=0; i<4; i++) {
			displays[i]=g.allEconomies[i+1];
			economies[i]=displays[i].economy;
		}
	}
	
	public void run() {
		boolean testing=false;
		while (true) {
			if (stop) break;
			if (gui.running) {
				for (int i=0; i<4; i++) {
					Economy economy=economies[i];
					EconomyDisplayPanels display=displays[i];
//					if (testing) System.out.println("TESTING FourEconomiesThread.run(). economy "+i+" is alive: "+display.economy.isAlive());
//					if (testing) System.out.println("TESTING FourEconomiesThread.run(). economy "+i+": ID="+display.economy.id);
					economy.run(true);
					gui.field_turn.setText(""+economy.turn);
					gui.label_turn.setText(""+economy.turn);	
				}
				for (int i=0; i<4; i++) {
					displays[i].updateDisplays();					
				}
				gui.validate();
			}
		}
	}

}
