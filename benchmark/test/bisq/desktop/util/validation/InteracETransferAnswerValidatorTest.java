/**
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */
package bisq.desktop.util.validation;


import org.junit.Assert;
import org.junit.Test;


public class InteracETransferAnswerValidatorTest {
    @Test
    public void validate() throws Exception {
        InteracETransferAnswerValidator validator = new InteracETransferAnswerValidator(new LengthValidator(), new RegexValidator());
        Assert.assertTrue(validator.validate("abcdefghijklmnopqrstuvwxy").isValid);
        Assert.assertTrue(validator.validate("ABCDEFGHIJKLMNOPQRSTUVWXY").isValid);
        Assert.assertTrue(validator.validate("1234567890").isValid);
        Assert.assertTrue(validator.validate("zZ-").isValid);
        Assert.assertFalse(validator.validate(null).isValid);// null

        Assert.assertFalse(validator.validate("").isValid);// empty

        Assert.assertFalse(validator.validate("two words").isValid);// two words

        Assert.assertFalse(validator.validate("ab").isValid);// too short

        Assert.assertFalse(validator.validate("abcdefghijklmnopqrstuvwxyz").isValid);// too long

        Assert.assertFalse(validator.validate("abc !@#").isValid);// invalid characters

    }
}
