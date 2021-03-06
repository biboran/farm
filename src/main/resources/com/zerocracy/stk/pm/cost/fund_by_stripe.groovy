/**
 * Copyright (c) 2016-2018 Zerocracy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to read
 * the Software only. Permissions is hereby NOT GRANTED to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.zerocracy.stk.pm.cost

import com.jcabi.xml.XML
import com.zerocracy.Par
import com.zerocracy.farm.Assume
import com.zerocracy.Project
import com.zerocracy.cash.Cash
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pm.ClaimOut
import com.zerocracy.pm.cost.Ledger

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Funded by Stripe')
  ClaimIn claim = new ClaimIn(xml)
  Cash amount = new Cash.S(claim.param('amount'))
  String customer = claim.param('stripe_customer')
  String email = claim.param('email')
  new Ledger(project).bootstrap().add(
    new Ledger.Transaction(
      amount,
      'assets', 'cash',
      'income', email.toLowerCase(Locale.ENGLISH),
      "Funded by Stripe customer \"${customer}\""
    )
  )
  new ClaimOut()
    .type('Notify project')
    .param('cause', claim.cid())
    .param(
      'message',
      new Par(
        'The project %s has been funded via Stripe for %s'
      ).say(project.pid(), amount)
    )
    .postTo(project)
  new ClaimOut().type('Notify user').token('user;yegor256').param(
    'message', new Par(
      'We just funded %s for %s via Stripe'
    ).say(project.pid(), amount)
  ).param('cause', claim.cid()).postTo(project)
}
