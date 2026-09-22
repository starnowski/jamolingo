---
name: root-prepare-impl-plan
description: Prepare the implementation plan based on the specification file
---
Extract the specification file name from the user's prompt.
Analyze the specification file. Based on your analyses prepare the implementation plan.
Write it into the markdown file. Named it similar as specification plan, just add the "_impl" suffix.
Make sure that implementation plan contains checkbox list, each checkbox represent if the specifc step was completed.

Naming implementation file strategy:
<example>
<specficationFile>101_DO_something.md</specficationFile>
<implementationFile>101_DO_something_impl.md</implementationFile>
</example>
