/*
 * Copyright 2015 Geeoz Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pawl.web.step

import org.openqa.selenium.WebDriver
import org.scalatest.concurrent.Eventually
import org.scalatest.exceptions.NotAllowedException
import pawl.Step
import pawl.web.{Context, WebPatience}

/** Enter text to web element.
  * @param text to enter
  * @param driver web driver to use
  */
final class Enter(text: String)
                 (implicit driver: WebDriver)
  extends Step[Context] with Eventually with WebPatience {
  private val context = new Context()

  override def execute(): Unit = {
    val selector = context.identity
    val by = context.locator.by
    selector match {
      case Some(s) => eventually {
        val element = driver.findElement(by.by(s))
        element.sendKeys(text)
      }
      case None => throw new NotAllowedException(
        s"For text $text please define selector of web element.", 0)
    }
  }

  override def clarification(): Context = context
}
