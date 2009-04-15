/***********************************************************************************
 * validation.js
 * Copyright (c) 2008 Sakai Project/Sakai Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.osedu.org/licenses/ECL-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/


// Matches each character against the 7 bit default alphabet as specified by GSM 03.38 and remove if invalid
function removeInvalidChars(msg) {
			var validExpr = new RegExp("[A-Za-z0-9@£$¥èéùìòÇ\n\r\fØøÅåΔ_ΦΓΛΩΠΨΣΘΞ^{}+|€ÆæßÉ!\"#¤%&'()* ,-./:;<=>?¡ÄÖÑÜ§¿äöñüà[~\\]\\\\]{1}");
			var validatedMsg = "";
			
			for (i=0;i<msg.length;i++) {
				var toCheck = msg.charAt(i);
				if (toCheck.match(validExpr)) {
					validatedMsg += toCheck;
				}
			}

			return validatedMsg;
}