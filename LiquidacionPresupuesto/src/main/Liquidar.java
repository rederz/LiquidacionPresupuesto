package main;

public class Liquidar {

	public static void main(String[] args) {

		LiquidacionPresupuestaria liquidar = new LiquidacionPresupuestaria();

		if (liquidar.liquidarPresupuesto()) {
			System.out.println("TERMINATED OK");
		} else {
			System.out.println("TERMINATED NOK");
		}

	}

}
