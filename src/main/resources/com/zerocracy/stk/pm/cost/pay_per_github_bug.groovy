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

import com.jcabi.github.Github
import com.jcabi.github.Issue
import com.jcabi.xml.XML
import com.zerocracy.Par
import com.zerocracy.entry.ExtGithub
import com.zerocracy.farm.Assume
import com.zerocracy.jstk.Farm
import com.zerocracy.jstk.Project
import com.zerocracy.jstk.cash.Cash
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pm.ClaimOut
import com.zerocracy.pm.cost.Rates
import com.zerocracy.pm.staff.Roles
import com.zerocracy.radars.github.Job

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Job was added to WBS')
  ClaimIn claim = new ClaimIn(xml)
  String job = claim.param('job')
  if (!job.startsWith('gh:')) {
    return
  }
  Farm farm = binding.variables.farm
  Github github = new ExtGithub(farm).value()
  Issue.Smart issue = new Issue.Smart(new Job.Issue(github, job))
  if (issue.pull) {
    return
  }
  String author = issue.author().login().toLowerCase(Locale.ENGLISH)
  Roles roles = new Roles(project).bootstrap()
  if (roles.hasAnyRole(author)) {
    Cash rate = Cash.ZERO
    Rates rates = new Rates(project).bootstrap()
    if (rates.exists(author)) {
      rate = rates.rate(author)
    }
    new ClaimOut()
      .type('Make payment')
      .param('job', job)
      .param('login', author)
      .param('reason', new Par('Bug was reported, see §29').say())
      .param('minutes', 15)
      .param('cash', rate.mul(15) / 60)
      .postTo(project)
  }
}
