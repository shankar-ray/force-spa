/**
 * Following parents to child can be nasty. That is why you need to ask special with xxxDetails.
 *
 * NamedRecord(or GraphicRecord) - id and name (for simple list display)
 * GuildUserBrief - Small subset (maybe just NamedRecord for some). Can be more for others. No relationship links.
 *
 * GuildUser - xxxBrief + remaining simple fields, plus xxxBrief for childToParent, and no parent to child
 * GuildUserDetails - xxx, plus xxx for childToParent and xxxDetails for parentToChild.
 *
 * above you see that childToParent is always one notch down in detail.
 * parentToChild is full details in xxxDetails for the purpose of getting full trees. Doing xxxDetails on a childToParent
 * link can be dangerous because too easy to explode to HUGE graph.
 *
 * GuildUserGraph
 *
 * When following parentToChild Don't include duplicate of childToParent reference in query!
 *
 * These associative things seem a little different. The rules for Brief vs. Details could be different because of how
 * they are used. Associate objects, when access through their parent, are always seen either standalone or in a details
 * view.  For raw queries on associate objects can filter out the entry you don't want by way you build query. For object
 * fetches that chain to the associative object we can make sure that the back reference is not fetched but rather just
 * references parent object.
 */
