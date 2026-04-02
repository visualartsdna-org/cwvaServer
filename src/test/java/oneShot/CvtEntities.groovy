package oneShot

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import rdf.JenaUtilities

class CvtEntities {

	def ju = new JenaUtilities()

	@Test
	void test() {
		cvt()
	}
	def files = [
		"0893d6af-80e5-4178-87a4-e1f6e92d4312.ttl",
		"08e8205a-190a-4668-92a6-4e0ef992f288.ttl",
		"0e5f5a2d-57d0-4714-8470-a51ff50217c6.ttl",
		"10dcaa19-956e-42db-98d4-40b9fcabd1a4.ttl",
		"16a5dd99-6a7e-49fb-8254-7785b676a2f4.ttl",
		"1fbca15a-88fb-4e9a-9a3e-e70c554d03c5.ttl",
		"2c0dd76b-3566-4dab-86f9-cfafa5490b8c.ttl",
		"3f02da48-d463-438f-b424-5d77a5ad5874.ttl",
		"3f525f51-c2c6-4415-bdf4-d6847f4d880a.ttl",
		"4621cd96-5bb6-4fdd-8d70-517cae3bae35.ttl",
		"4db1743c-47ce-4c39-8ae6-38f5ce5d6c8c.ttl",
		"52ed839f-a300-4073-8893-25643c9bab65.ttl",
		"53bfd31d-23ed-460f-b7f4-61affe535e61.ttl",
		"53da6b4b-7e14-4f6e-bb55-6dd7147b9be1.ttl",
		"552759c6-b155-499f-8709-6d7db265ca9b.ttl",
		"5caae6ea-2f5b-48c2-9efb-2b65688237ea.ttl",
		"5e053ee5-231b-4899-b981-f16e26b93b0b.ttl",
		"6b6bc80c-e9c4-43ed-88be-fd5569af39a2.ttl",
		"6b8c3d25-2a2b-4fee-86b4-67d62c41ae70.ttl",
		"7349c01c-ef13-40ff-b310-810cd503607b.ttl",
		"769448d1-c62f-4d45-a5f6-ece9403cf261.ttl",
		"855471c8-7e71-4172-99f2-5405682c2189.ttl",
		"88ae3a7d-94f0-4a51-8a18-7445f2f74603.ttl",
		"89af0c40-c4e9-4f70-b05e-88b34af25004.ttl",
		"89b0f16d-a04e-4a48-ba07-8d7ad015b430.ttl",
		"89e2d4ea-6b35-4de3-a2f0-132f8588009e.ttl",
		"b06f2cdf-546a-4246-91bc-754fbeb7861b.ttl",
		"b1394ace-9823-4244-9e2c-c7b84771a6ef.ttl",
		"b666978a-8584-40b0-8e68-29bb1edf40f4.ttl",
		"bd9fa940-20ea-4f6b-927a-13cf2c83f792.ttl",
		"cffdbd48-bb58-4efa-81fa-89e66c282b2d.ttl",
		"d37e82c6-2af0-46ab-aae3-4a0bf14869a9.ttl",
		"d4e04b45-83d0-4a9e-9704-29738bb8dfd1.ttl",
		"e62b67d3-3a9a-486d-934d-52022582011d.ttl",
	]

	def src = "C:/stage/server/cwvaContent/ttl/data"
	def tgt = "/stage/data"
	def cvt() {

		files.each{
			def model = ju.loadFiles("$src/$it")
			ju.queryExecUpdate(model,rdf.Prefixes.forQuery,"""
	delete { ?s the:topic ?t }
	insert { ?s the:topic the:Criticism }
	where {
		?s the:topic ?t
		}
""")
			ju.saveModelFile(model,"$tgt/$it","ttl")
		}
	}

	def dir = "/stage/data"
	def cvt0() {

		def model = ju.loadFiles(dir)
		ju.queryExecUpdate(model,rdf.Prefixes.forQuery,"""
	delete { ?s the:topic ?t }
	insert { ?s the:topic the:Criticism }
	where {
		?s the:topic ?t
		}
""")
		ju.saveModelFile(model,"$dir/entities.ttl","ttl")
	}
}
