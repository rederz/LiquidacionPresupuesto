package main;

import utils.CLogger;

public class Liquidar {

	public static void main(String[] args) {
		CLogger.writeConsole("START");

		LiquidacionPresupuestaria liquidar = new LiquidacionPresupuestaria();

		if (liquidar.liquidarPresupuesto()) {
			CLogger.writeConsole("TERMINATED OK");
		} else {
			CLogger.writeConsole("TERMINATED NOK");
		}

	}

}
