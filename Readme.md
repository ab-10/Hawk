# Hawk

Hawk aims to provide transparent and explainable functionality for obtaining properties of words and determining whether triples of words are _discriminative_.

## What constitutes a discriminative triple?

We can affirm said triple is discriminative if the feature allows to discriminate the pivot from the comparison, that is, if it applies to the pivot but not to the comparison. Here's an example:

	- Pivot: Paris
	- Comparison: Barcelona
	- Feature: French

This is a discriminative triple: French applies to Paris because Paris is a French city, but not to Barcelona, since it isn't.It is important to mention that as soon as the feature and the comparison are related, the triple is not discriminative. Therefore, a triple where the feature does not apply to the pivot but to the comparison is still not discriminative.

## How can I use it?
The best way to use Hawk is through the API.

### Running a Local Server Instance

0. Download the file from https://github.com/ab-10/Hawk/tree/v0.1.0

1. Extract the file 
`tar -xf hawk-0.1.0.tar.gz`

2. And run the jetty server
`java -jar hawk-0.1.0/hawk-0.1.0-jar-with-dependencies.jar hawk-0.1.0/indexes`

3. Now the Hawk API can be accessed on `localhost:8080`, see [API Usage](https://github.com/ab-10/Hawk#api-usage) for more information.



### API Usage

Graphical front end can be accessed by pointing your browser to the address _without_ `/api`.
E.g. to access properties front end after running the server on your machine point your browser to `localhost:8080/properties` and for a programmatic API send requests to `localhost:8080/properties/api`.

#### Obtaining Properties

Returns a list of properties with their roles in parenthesis, organized by source.
For example (shortened):

```
{"WKP_Graph":["Hawk(definiendum)", "an(O)","unincorporated(B-differentia-quality)","community(B-supertype)","in(B-differentia-quality)"]
"WKT": ["hawk(definiendum)","diurnal(has_diff_qual)","of the family Accipitridae(has_origin_loc)","predatory_bird(has_supertype)"]}
```

Accessible on `HOST_NAME/properties/api` and requires 3 parameters:

1. `properties`, which properties do you want?
Accepted values:
`p` for `pivot`'s properties
`c` for `comparison`'s properties
`p-c` `pivot`'s minus `comparison`'s
`c-p` `comparison`'s minus `pivot`'s
`intersection` the common properties of `pivot` and `comparison`.

2. `pivot` string value of pivot

3. `comparison` string value of comparison

#### Determining Discriminativity

Returns two element lists, organized by source `[DECISION, JUSTIFICATION]`, where `DECISION` is `"true"`/`"false"` and `JUSTIFICATION` is a natural language justification for the decision.
For example:

```
{"WKP_Graph":["false","Because hawk and eagle don't contain bird as a property"],"WKT":["false","Because hawk and eagle don't contain bird as a property"],"WN":["true","Because hawk contains bird in property of has_supertype and has_supertype role and eagle doesn't contain bird as one of its properties."]}
```

Accessible on `HOST_NAME/roleBasedVote/api` requires 3 parameters:
`pivot`, `comparison`, `feature`
and returns whether `pivot` and `feature` have a common property, that `comparison` doesn't have (the role of the property as well as the value is taken into account under this comparison type).

## How can I contribute?
First of all thanks for showing interest in Hawk!
These are the recommended steps for contributing:
1. Play around with Hawk. 
Take a look at the usage instructions above and try building something (think hackathon-level project). In case you produce something decent, we'd be happy to list it as an example (thus accepting your first contribution), however the point here is to get you somewhat familiar with the project.
2. After having the time of your life, completing the step (1.), you now have a dozen feature ideas (or at least a single one), that would make Hawk even more amazing. So what are you waiting for? Fork the project, implement your ideas and I'll see you at the PR!

_P.S. if you need any help during any of this, see the support section!_

## Support
For help shoot an email to Armins(dot)Bagrats at gmail, and I'll (hopefully) respond.
