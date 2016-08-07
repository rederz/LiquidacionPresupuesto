package main;

import java.sql.ResultSet;
import java.sql.SQLException;

import data.Database;
import utils.CLogger;

public class LiquidacionPresupuestaria {

	private static final String QUERY_GASTOS = "select * from GASTOS where fuente = 29 and organismo = 101 and correlativo = 2 order by fec_aprobado";
	private static final String QUERY_PAGOS = "select * from ISCV order by fecha_recaudo";
	private static final String INSERT_LIQUIDACION = "INSERT INTO gasto_liquidado (documento,porcentaje,ayuda_a) VALUES (?,?,?)";

	private Database dbGastos;
	private Database dbPagos;
	private Database dbLiquidacion;

	public boolean liquidarPresupuesto() {

		dbGastos = new Database();
		dbPagos = new Database();
		dbLiquidacion = new Database();

		try {

			if (dbGastos.isOpen() && dbPagos.isOpen() && dbLiquidacion.isOpen()) {

				ResultSet resultGastos = dbGastos.runQuery(QUERY_GASTOS);
				ResultSet resultPagos = dbPagos.runQuery(QUERY_PAGOS);

				Double total = 0.0;
				Double saldo = 0.0;
				Double pago = 0.0;

				dbLiquidacion.setSqlInsert(INSERT_LIQUIDACION);

				CLogger.writeConsole(String.format("%1$20s %2$20s %3$20s %4$40s %5$20s %6$20s", "GASTO", "ISCV",
						"SALDO", "AYUDOA", "DOCUMENTO", "EN UN (%)"));

				realizarPago(pago, total, saldo, resultPagos, resultGastos);

				return true;
			}
		} catch (Exception e) {
			CLogger.writeFullConsole("ERROR-EXECUTION: liquidarPresupuesto(): ", e);
		} finally {
			try {
				dbGastos.close();
				dbPagos.close();
				dbLiquidacion.close();
			} catch (SQLException e) {
				CLogger.writeFullConsole("ERROR-CLOSE: liquidarPresupuesto(): ", e);
			}

		}

		return false;

	}

	private void realizarPago(Double pago, Double total, Double saldo, ResultSet resultPagos, ResultSet resultGastos)
			throws SQLException {

		boolean hayGastos = true;
		if (saldo.doubleValue() == 0.0) {
			if (resultGastos.next()) {
				total = resultGastos.getDouble("monto_renglon");
				saldo = total;
			} else {
				hayGastos = false;
				CLogger.writeConsole("No hay mas gastos en la base de datos");
			}
		}

		boolean hayPagos = true;
		if (pago.doubleValue() == 0.0) {
			if (resultPagos.next()) {
				pago = resultPagos.getDouble("valor_pago");
			} else {
				hayPagos = true;
				CLogger.writeConsole("No hay mas pagos en la base de datos");
			}
		}

		if (hayPagos && hayGastos) {

			Double tempSaldo = saldo.doubleValue() - pago.doubleValue();

			if (tempSaldo.doubleValue() >= 0) {
				dbLiquidacion.addBatch(resultPagos.getDouble("DOCUMENTO"), (pago / total * 100),
						resultGastos.getString("ACTIVIDAD_OBRA_NOMBRE"));

				CLogger.writeConsole(String.format("%1$20.2f %2$20.2f %3$20.2f %4$40s %5$20.0f %6$20.2f", total, pago,
						tempSaldo, resultGastos.getString("ACTIVIDAD_OBRA_NOMBRE"), resultPagos.getDouble("DOCUMENTO"),
						(pago / total * 100)));
				saldo = tempSaldo.doubleValue();
				pago = 0.0;
			} else {
				dbLiquidacion.addBatch(resultPagos.getDouble("DOCUMENTO"), (saldo / total * 100),
						resultGastos.getString("ACTIVIDAD_OBRA_NOMBRE"));

				CLogger.writeConsole(String.format("%1$20.2f %2$20.2f %3$20.2f %4$40s %5$20.0f %6$20.2f", total, saldo,
						0.0, resultGastos.getString("ACTIVIDAD_OBRA_NOMBRE"), resultPagos.getDouble("DOCUMENTO"),
						(saldo / total * 100)));
				pago = pago.doubleValue() - saldo.doubleValue();
				saldo = 0.0;
			}

			realizarPago(pago, total, saldo, resultPagos, resultGastos);
		}

	}

}
