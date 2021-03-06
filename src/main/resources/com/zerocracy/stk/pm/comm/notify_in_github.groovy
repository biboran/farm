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
package com.zerocracy.stk.pm.comm

import com.jcabi.github.Comment
import com.jcabi.github.Coordinates
import com.jcabi.github.Github
import com.jcabi.github.Issue
import com.jcabi.github.Repo
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.entry.ExtGithub
import com.zerocracy.farm.Assume
import com.zerocracy.farm.props.Props
import com.zerocracy.pm.ClaimIn
import com.zerocracy.radars.github.GhTube
import com.zerocracy.radars.github.Quota
import com.zerocracy.radars.github.ThrottledComments
import java.util.concurrent.TimeUnit

// Token must look like: zerocracy/farm;123;6
//   - repository coordinates
//   - issue cid
//   - comment cid inside the issue (optional)

def exec(Project project, XML xml) {
  new Assume(project, xml).type('Notify in GitHub')
  ClaimIn claim = new ClaimIn(xml)
  String[] parts = claim.token().split(';')
  if (parts[0] != 'github') {
    throw new IllegalArgumentException(
      "Something is wrong with this token: ${claim.token()}"
    )
  }
  Farm farm = binding.variables.farm
  Github github = new ExtGithub(farm).value()
  if (new Quota(github).over()) {
    claim.copy().until(TimeUnit.MINUTES.toSeconds(5L)).postTo(project)
    return
  }
  Repo repo = github.repos().get(
    new Coordinates.Simple(parts[1])
  )
  Props props = new Props(farm)
  String message = String.format(
    '%s\n\n<!-- claim_id: %d, version: %s, hash: %s -->',
    claim.param('message'),
    claim.cid(),
    props.get('//build/version', ''),
    props.get('//build/revision', '')
  )
  Issue issue = repo.issues().get(
    Integer.parseInt(parts[2])
  )
  if (parts.length > 3) {
    Comment comment = issue.comments().get(
      Integer.parseInt(parts[3])
    )
    new GhTube(comment).say(message)
  } else {
    new ThrottledComments(issue.comments()).post(message)
  }
}
