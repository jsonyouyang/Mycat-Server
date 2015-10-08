/*
 * Copyright (c) 2013, OpenCloudDB/MyCAT and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software;Designed and Developed mainly by many Chinese 
 * opensource volunteers. you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License version 2 only, as published by the
 * Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Any questions about this component can be directed to it's project Web address 
 * https://code.google.com/p/opencloudb/.
 *
 */
package io.mycat.server.response;

import io.mycat.net.BufferArray;
import io.mycat.net.NetSystem;
import io.mycat.server.Fields;
import io.mycat.server.MySQLFrontConnection;
import io.mycat.server.packet.EOFPacket;
import io.mycat.server.packet.FieldPacket;
import io.mycat.server.packet.ResultSetHeaderPacket;
import io.mycat.server.packet.util.PacketUtil;

/**
 * 查询执行时间超过设定阈值的SQL
 * 
 * @author mycat
 */
public final class ShowSQLSlow {

	private static final int FIELD_COUNT = 7;
	private static final ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
	private static final FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
	private static final EOFPacket eof = new EOFPacket();
	static {
		int i = 0;
		byte packetId = 0;
		header.packetId = ++packetId;

		fields[i] = PacketUtil.getField("HOST", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;

		fields[i] = PacketUtil.getField("SCHEMA", Fields.FIELD_TYPE_LONG);
		fields[i++].packetId = ++packetId;

		fields[i] = PacketUtil.getField("DATASOURCE", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;

		fields[i] = PacketUtil.getField("INDEX", Fields.FIELD_TYPE_LONG);
		fields[i++].packetId = ++packetId;

		fields[i] = PacketUtil.getField("START_TIME", Fields.FIELD_TYPE_LONGLONG);
		fields[i++].packetId = ++packetId;

		fields[i] = PacketUtil.getField("EXECUTE_TIME", Fields.FIELD_TYPE_LONGLONG);
		fields[i++].packetId = ++packetId;

		fields[i] = PacketUtil.getField("SQL", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;

		eof.packetId = ++packetId;
	}

	public static void execute(MySQLFrontConnection c) {
		BufferArray bufferArray = NetSystem.getInstance().getBufferPool().allocateArray();

		// write header
		header.write(bufferArray);

		// write fields
		for (FieldPacket field : fields) {
			field.write(bufferArray);
		}

		// write eof
		eof.write(bufferArray);

		// write rows
		byte packetId = eof.packetId;
		
		//TODO COOLLF 订单等待修正
//		SQLRecord[] records = MycatServer.getInstance().getSqlRecorder().getRecords();
//		for (int i = records.length - 1; i >= 0; i--) {
//			if (records[i] != null) {
//				RowDataPacket row = getRow(records[i], c.getCharset());
//				row.packetId = ++packetId;
//				row.write(bufferArray);
//			}
//		}

		// write last eof
		EOFPacket lastEof = new EOFPacket();
		lastEof.packetId = ++packetId;
		lastEof.write(bufferArray);

		// write bufferArray
		c.write(bufferArray);
	}

//	private static RowDataPacket getRow(SQLRecord sql, String charset) {
//		RowDataPacket row = new RowDataPacket(FIELD_COUNT);
//		row.add(StringUtil.encode(sql.host, charset));
//		row.add(StringUtil.encode(sql.schema, charset));
//		row.add(StringUtil.encode(sql.dataNode, charset));
//		row.add(IntegerUtil.toBytes(sql.dataNodeIndex));
//		row.add(LongUtil.toBytes(sql.startTime));
//		row.add(LongUtil.toBytes(sql.executeTime));
//		row.add(StringUtil.encode(sql.statement, charset));
//		return row;
//	}

}