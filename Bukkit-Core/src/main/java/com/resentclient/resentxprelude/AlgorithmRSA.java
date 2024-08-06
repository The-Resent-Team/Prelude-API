/*
 * Prelude-API is a plugin to implement features for the Client.
 * Copyright (C) 2024 cire3, Preva1l
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.resentclient.resentxprelude;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

/*
* For verification (most skids won't be able to bypass this and potentially abuse Prelude),
* We send a random message encrypted with the client's public key
* The client decrypts the message and returns "RESPREVER" + message + (sha256hash of the sent message encrypted with private key)
* It then signs the message with the private key, which is verified
* on the plugin to enable Prelude features :D
*
* NOTE: we don't use official packets in Prelude-Protocol, we use normal C17 packets
* */
public class AlgorithmRSA {
    public static final BigInteger PRELUDE_CLIENT_PUBLIC_N = new BigInteger("568481805341850075300913081261354695145118843764377822291878169381553385453526802667764472053067027964172413628723944022946642603384304204713210112433689198436217481487937248243287897839914841494646361648206936043071562251564422096391285580100522242933054836931665680049371512669561710094305716999584041396134339269491014445581075539062922313104183198890336018500979583992341241634003348017465882517634579227124117727037641239807783926572359181670310094083441139059569664325708701283696515902940078820515456463541545282398846414369852630887599003352072184663079922210962284718862866937697628566286409815250807251624320837122423731105153135447140666848274568803965881815074509298234678339446587931138029109798893240695773885859225515718325100224895582260971569050913969090688788262203995319768807382185211283777896349243251398684348971603248468903764003872133387072929361814137894773478178990942913342146752359141704092979091957011838580243631379106711202896989550389601549037108389873556022583971852854593701914544272101008421240436949343293821332958844712457004079539530205815742199030339520684756660735412424661825299947511080775803064499232544365984644325952266967004903410233289236378172664219327489319617953360220306726185102633");
    public static final BigInteger PRELUDE_CLIENT_PUBLIC_E = new BigInteger("65537");

    public static BigInteger encrypt(BigInteger message, BigInteger e, BigInteger n) {
        return message.modPow(e, n);
    }

    public static BigInteger decrypt(BigInteger message, BigInteger d, BigInteger n) {
        return message.modPow(d, n);
    }

    public static BigInteger bytesToCipher(byte[] bytes) {
        return new BigInteger(bytes);
    }

    public static byte[] cipherToBytes(BigInteger message) {
        return message.toByteArray();
    }

    public static BigInteger stringToCipher(String message) {
        return new BigInteger(message.getBytes(StandardCharsets.US_ASCII));
    }

    public static String cipherToString(BigInteger message) {
        return new String(message.toByteArray());
    }

    static {

    }
}
